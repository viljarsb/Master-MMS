package MMS.EdgeRouter.WsManagement;

import MMS.Client.Exceptions.MMTPValidationException;
import MMS.EdgeRouter.MessageForwarding.MessageForwardingEngine;
import MMS.EdgeRouter.SubscriptionManager.SubscriptionManager;
import MMS.EdgeRouter.ThreadPoolService;
import MMS.Protocols.MMTP.MessageFormats.*;
import MMS.Protocols.MMTP.Validators.MMTPValidator;
import com.google.protobuf.InvalidProtocolBufferException;
import net.maritimeconnectivity.pki.PKIIdentity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;

import java.util.List;


/**
 * The {@code MessageHandler} class is responsible for processing messages received
 * from WebSocket sessions. It utilizes a worker thread pool to process messages in parallel
 * and handles different types of messages based on the MMTP (Maritime Messaging Transfer Protocol).
 */
public class MessageHandler
{
    private static final Logger logger = LogManager.getLogger(MessageHandler.class);
    private static MessageHandler instance = new MessageHandler();

    private final SubscriptionManager subscriptionManager = SubscriptionManager.getInstance();
    private final MessageForwardingEngine messageForwardingEngine = MessageForwardingEngine.getInstance();
    private final ConnectionHandler connectionHandler = ConnectionHandler.getInstance();


    /**
     * Instantiates a new {@code MessageHandler} and initializes its components.
     */
    private MessageHandler()
    {
        logger.info("Message handler created");
    }


    /**
     * Returns the singleton instance of the {@code MessageHandler}.
     *
     * @return the singleton instance of the {@code MessageHandler}
     */
    public synchronized static MessageHandler getInstance()
    {
        if (instance == null)
            instance = new MessageHandler();

        return instance;
    }


    /**
     * Submits a message processing task to the worker thread pool for asynchronous processing.
     * This method is responsible for handling incoming messages and dispatching them
     * to be processed in parallel by the worker thread pool.
     *
     * @param payload the payload of the received message
     * @param offset  the starting offset in the payload
     * @param len     the length of the message
     * @param session the WebSocket session from which the message was received
     */
    public void handleMessage(byte[] payload, int offset, int len, Session session)
    {
        ThreadPoolService.executeAsync(() -> processMessage(payload, offset, len, session));
    }


    /**
     * Processes the received message according to its type and delegates further processing
     * to the appropriate methods.
     *
     * @param payload the payload of the received message
     * @param offset  the starting offset in the payload
     * @param len     the length of the message
     * @param session the WebSocket session from which the message was received
     */
    private void processMessage(byte[] payload, int offset, int len, Session session)
    {
        byte[] message = new byte[len];
        System.arraycopy(payload, offset, message, 0, len);

        ConnectionState state = connectionHandler.getConnectionState(session);

        try
        {
            ProtocolMessage protocolMessage = ProtocolMessage.parseFrom(message);
            byte[] content = protocolMessage.getContent().toByteArray();
            MessageType messageType = protocolMessage.getType();

            switch (messageType)
            {
                case DIRECT_APPLICATION_MESSAGE, SUBJECT_CAST_APPLICATION_MESSAGE -> processApplicationMessage(messageType, content, state);
                case REGISTER, UNREGISTER -> processRegistrationMessage(messageType, content, state);
                case UNRECOGNIZED -> handleUnrecognizedMessage(state);
            }
        }

        catch (InvalidProtocolBufferException ex)
        {
            logger.warn("Agent ID = {}: Received invalid protocol buffer, closing session. Reason: {}", state.getAgentId(), ex.getMessage());
            state.getSession().close(StatusCode.PROTOCOL, "Sent invalid protocol buffer");
        }

        catch (MMTPValidationException e)
        {
            logger.warn("Agent ID = {}: Received invalid message according to MMTP specification, closing session. Reason: {}", state.getAgentId(), e.getMessage());
            state.getSession().close(StatusCode.PROTOCOL, "Sent invalid message according to MMTP specification");
        }
    }


    /**
     * Processes application messages of type DIRECT_APPLICATION_MESSAGE and SUBJECT_CAST_APPLICATION_MESSAGE,
     * handling message validation and forwarding to the forwarding engine who is responsible to send the messages.
     *
     * @param type    the type of application message (either DIRECT_APPLICATION_MESSAGE or SUBJECT_CAST_APPLICATION_MESSAGE)
     * @param content the content of the application message in bytes
     * @param state   the ConnectionState associated with the WebSocket session
     * @throws InvalidProtocolBufferException if the message content cannot be parsed as a valid protobuf message
     * @throws MMTPValidationException        if the message fails validation according to MMTP rules
     */
    private void processApplicationMessage(MessageType type, byte[] content, ConnectionState state) throws InvalidProtocolBufferException, MMTPValidationException
    {
        PKIIdentity identity = state.getIdentity();

        if (identity == null)
        {
            logger.warn("Agent ID = {}: Received application message from unauthenticated session, closing session", state.getAgentId());
            state.getSession().close(StatusCode.POLICY_VIOLATION, "Attempted to send application message from unauthenticated session");
            return;
        }

        switch (type)
        {
            case DIRECT_APPLICATION_MESSAGE -> processDirectApplicationMessage(content, state);
            case SUBJECT_CAST_APPLICATION_MESSAGE -> processSubjectCastApplicationMessage(content, state);
            default ->
            {
                logger.warn("Agent ID = {}: Received unrecognized application message, closing session", state.getAgentId());
                state.getSession().close(StatusCode.PROTOCOL, "Sent unrecognized application message");
            }
        }
    }


    /**
     * Processes DIRECT_APPLICATION_MESSAGE messages by parsing, validating, and forwarding them.
     *
     * @param content the content of the direct application message in bytes
     * @param state   the ConnectionState associated with the WebSocket session
     * @throws InvalidProtocolBufferException if the message content cannot be parsed as a valid protobuf message
     * @throws MMTPValidationException        if the message fails validation according to MMTPValidator rules
     */
    private void processDirectApplicationMessage(byte[] content, ConnectionState state) throws InvalidProtocolBufferException, MMTPValidationException
    {
        logger.debug("Agent ID = {}: Received direct application message", state.getAgentId());

        DirectApplicationMessage directApplicationMessage = DirectApplicationMessage.parseFrom(content);
        MMTPValidator.validate(directApplicationMessage);

        String sender = state.getIdentity().getMrn();
        String presentSender = directApplicationMessage.getSender();

        if (!sender.equals(presentSender))
        {
            logger.warn("Agent ID = {}: Received direct application message with different identity, closing session", state.getAgentId());
            state.getSession().close(StatusCode.POLICY_VIOLATION, "Attempted to send direct application message with different identity");
            return;
        }

        messageForwardingEngine.forwardMessage(directApplicationMessage);
    }


    /**
     * Processes SUBJECT_CAST_APPLICATION_MESSAGE messages by parsing, validating, and forwarding them.
     *
     * @param content the content of the subject-cast application message in bytes
     * @param state   the ConnectionState associated with the WebSocket session
     * @throws InvalidProtocolBufferException if the message content cannot be parsed as a valid protobuf message
     * @throws MMTPValidationException        if the message fails validation according to MMTPValidator rules
     */
    private void processSubjectCastApplicationMessage(byte[] content, ConnectionState state) throws InvalidProtocolBufferException, MMTPValidationException
    {
        logger.debug("Agent ID = {}: Received subject-cast application message", state.getAgentId());

        SubjectCastApplicationMessage subjectCastApplicationMessage = SubjectCastApplicationMessage.parseFrom(content);
        MMTPValidator.validate(subjectCastApplicationMessage);

        String sender = state.getIdentity().getMrn();
        String presentSender = subjectCastApplicationMessage.getSender();

        if (!sender.equals(presentSender))
        {
            logger.warn("Agent ID = {}: Received subject-cast application message with different identity, closing session", state.getAgentId());
            state.getSession().close(StatusCode.POLICY_VIOLATION, "Attempted to send subject-cast application message with different identity");
            return;
        }

        messageForwardingEngine.forwardMessage(subjectCastApplicationMessage);
    }


    /**
     * Processes REGISTER and UNREGISTER messages by calling the appropriate method.
     *
     * @param content the content of the register message in bytes
     * @param state   the ConnectionState associated with the WebSocket session
     * @throws InvalidProtocolBufferException if the message content cannot be parsed as a valid protobuf message
     * @throws MMTPValidationException        if the message fails validation according to MMTPValidator rules
     */
    private void processRegistrationMessage(MessageType type, byte[] content, ConnectionState state) throws InvalidProtocolBufferException, MMTPValidationException
    {
        switch (type)
        {
            case REGISTER -> processRegisterMessage(content, state);
            case UNREGISTER -> processUnregisterMessage(content, state);
        }
    }


    /**
     * Processes REGISTER messages by parsing, validating, and updating subscriptions.
     *
     * @param content the content of the register message in bytes
     * @param state   the ConnectionState associated with the WebSocket session
     * @throws InvalidProtocolBufferException if the message content cannot be parsed as a valid protobuf message
     * @throws MMTPValidationException        if the message fails validation according to MMTPValidator rules
     */
    private void processRegisterMessage(byte[] content, ConnectionState state) throws InvalidProtocolBufferException, MMTPValidationException
    {
        logger.debug("Agent ID = {}: Received register message", state.getAgentId());
        Register register = Register.parseFrom(content);
        MMTPValidator.validate(register);

        List<String> subjects = register.getInterestsList();
        subscriptionManager.addSubscription(subjects, state.getSession());

        if (register.hasWantDirectMessages())
        {
            subscriptionManager.addDirectMessageSubscription(state.getSession());
        }
    }


    /**
     * Processes UNREGISTER messages by parsing, validating, and updating subscriptions.
     *
     * @param content the content of the unregister message in bytes
     * @param state   the ConnectionState associated with the WebSocket session
     * @throws InvalidProtocolBufferException if the message content cannot be parsed as a valid protobuf message
     * @throws MMTPValidationException        if the message fails validation according to MMTPValidator rules
     */
    private void processUnregisterMessage(byte[] content, ConnectionState state) throws InvalidProtocolBufferException, MMTPValidationException
    {
        logger.debug("Agent ID = {}: Received unregister message", state.getAgentId());
        Unregister unregister = Unregister.parseFrom(content);
        MMTPValidator.validate(unregister);

        List<String> subjects = unregister.getInterestsList();
        subscriptionManager.removeSubscription(subjects, state.getSession());

        if (unregister.hasWantDirectMessages())
        {
            subscriptionManager.removeDirectMessageSubscription(state.getSession());
        }
    }


    /**
     * Handles unrecognized messages by logging an error and closing the WebSocket
     * session with a protocol error status code.
     *
     * @param state the ConnectionState associated with the WebSocket session
     */
    private void handleUnrecognizedMessage(ConnectionState state)
    {
        logger.warn("Received unrecognized message, that does not conform to the MMTP specification, from Agent ID = " + state.getAgentId() + " , closing session");
        state.getSession().close(StatusCode.PROTOCOL, "Received unrecognized message, that does not conform to the MMTP specification");
    }
}
