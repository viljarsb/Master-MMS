package MMS.EdgeRouter.WsManagement;

import MMS.Client.Exceptions.MMTPValidationException;
import MMS.EdgeRouter.MessageForwarding.MessageForwardingEngine;
import MMS.EdgeRouter.SubscriptionManager.SubscriptionManager;
import MMS.Protocols.MMTP.MessageFormats.*;
import MMS.Protocols.MMTP.Validators.MMTPValidator;
import com.google.protobuf.InvalidProtocolBufferException;
import net.maritimeconnectivity.pki.PKIIdentity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageHandler
{
    private static final Logger logger = LogManager.getLogger(MessageHandler.class);
    private static final SubscriptionManager subscriptionManager = SubscriptionManager.getInstance();
    private static final MessageForwardingEngine messageForwardingEngine = MessageForwardingEngine.getInstance();
    private static final ConnectionHandler connectionHandler = ConnectionHandler.getInstance();
    private static final ExecutorService workerPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


    /**
     * Submits a message processing task to the worker thread pool.
     *
     * @param payload  the payload of the received message
     * @param offset   the starting offset in the payload
     * @param len      the length of the message
     * @param session  the WebSocket session from which the message was received
     */
    public void handleMessage(byte[] payload, int offset, int len, Session session)
    {
        workerPool.execute(() -> processMessage(payload, offset, len, session));
    }


    /**
     * Processes the received message according to its type.
     *
     * @param payload  the payload of the received message
     * @param offset   the starting offset in the payload
     * @param len      the length of the message
     * @param session  the WebSocket session from which the message was received
     */
    private void processMessage(byte[] payload, int offset, int len, Session session)
    {
        byte[] message = new byte[len];
        System.arraycopy(payload, offset, message, 0, len);

        try
        {
            ProtocolMessage protocolMessage = ProtocolMessage.parseFrom(message);
            MessageType messageType = protocolMessage.getType();

            switch (messageType)
            {
                case DIRECT_APPLICATION_MESSAGE, SUBJECT_CAST_APPLICATION_MESSAGE -> processApplicationMessage(messageType, protocolMessage.getContent().toByteArray(), session);
                case REGISTER, UNREGISTER -> processRegisterMessage(messageType, protocolMessage.getContent().toByteArray(), session);
                case UNRECOGNIZED -> handleUnrecognizedMessage(session);
            }
        }

        catch (InvalidProtocolBufferException ex)
        {
            logger.error("Received invalid protocol buffer message: " + ex.getMessage());
        }

        catch (MMTPValidationException e)
        {
            logger.error("MMTP validation failed: " + e.getMessage());
        }
    }


    /**
     * Processes application messages of type DIRECT_APPLICATION_MESSAGE and
     * SUBJECT_CAST_APPLICATION_MESSAGE.
     *
     * @param type     the type of application message
     * @param content  the content of the application message
     * @param session  the WebSocket session from which the message was received
     * @throws InvalidProtocolBufferException if the message content cannot be parsed
     * @throws MMTPValidationException if the message validation fails
     */
    private void processApplicationMessage(MessageType type, byte[] content, Session session) throws InvalidProtocolBufferException, MMTPValidationException
    {
        if (type == MessageType.DIRECT_APPLICATION_MESSAGE)
        {
            logger.info("Received direct application message from agent");
            DirectApplicationMessage directApplicationMessage = DirectApplicationMessage.parseFrom(content);
            MMTPValidator.validate(directApplicationMessage);

            ConnectionState connectionState = connectionHandler.getConnectionState(session);
            PKIIdentity pkiIdentity = connectionState.getIdentity();
            if(pkiIdentity == null)
            {
                logger.error("Received direct application message from unauthenticated session, dropping message");
                session.close(StatusCode.POLICY_VIOLATION, "Attempted to send direct application message from unauthenticated session");
            }

            else
            {
                String sender = pkiIdentity.getMrn();
                String presentSender = directApplicationMessage.getSender();

                if(!sender.equals(presentSender))
                {
                    logger.error("Received direct application message from session with different identity, dropping message");
                    session.close(StatusCode.POLICY_VIOLATION, "Attempted to send direct application message with different identity");
                    return;
                }

                messageForwardingEngine.forwardMessage(directApplicationMessage);
            }
        }

        else if (type == MessageType.SUBJECT_CAST_APPLICATION_MESSAGE)
        {
            logger.info("Received subject cast application message");
            SubjectCastApplicationMessage subjectCastApplicationMessage = SubjectCastApplicationMessage.parseFrom(content);
            MMTPValidator.validate(subjectCastApplicationMessage);

            ConnectionState connectionState = connectionHandler.getConnectionState(session);
            PKIIdentity pkiIdentity = connectionState.getIdentity();
            if(pkiIdentity == null)
            {
                logger.error("Received direct application message from unauthenticated session, dropping message");
                session.close(StatusCode.POLICY_VIOLATION, "Attempted to send direct application message from unauthenticated session");
            }

            else
            {
                String sender = pkiIdentity.getMrn();
                String presentSender = subjectCastApplicationMessage.getSender();

                if(!sender.equals(presentSender))
                {
                    logger.error("Received subject-cast application message from session with different identity, dropping message");
                    session.close(StatusCode.POLICY_VIOLATION, "Attempted to send subject-cast application message with different identity");
                    return;
                }

                messageForwardingEngine.forwardMessage(subjectCastApplicationMessage);
            }
        }

        else
        {
            logger.error("Received unrecognized application message");
        }
    }


    /**
     * Processes register and unregister messages.
     *
     * @param type     the type of the register message
     * @param content  the content of the register message
     * @param session  the WebSocket session from which the message was received
     * @throws InvalidProtocolBufferException if the message content cannot be parsed
     */
    private void processRegisterMessage(MessageType type, byte[] content, Session session) throws InvalidProtocolBufferException
    {
        if (type == MessageType.REGISTER)
        {
            logger.info("Received register message");
            Register register = Register.parseFrom(content);
            MMTPValidator.validate(register);

            List<String> subjects = register.getInterestsList();

            subscriptionManager.addSubscription(subjects, session);

            if(register.hasWantDirectMessages())
                subscriptionManager.addDirectMessageSubscription(session);
        }


        else if(type == MessageType.UNREGISTER)
        {
            logger.info("Received unregister message");
            Unregister unregister = Unregister.parseFrom(content);
            MMTPValidator.validate(unregister);

            List<String> subjects = unregister.getInterestsList();
            subscriptionManager.removeSubscription(subjects, session);

            if(unregister.hasWantDirectMessages())
                subscriptionManager.removeDirectMessageSubscription(session);
        }

        else
        {
            logger.error("Received unrecognized register message");
        }
    }


    /**
     * Handles unrecognized messages by logging an error and closing the WebSocket
     * session with a protocol error status code.
     *
     * @param session the WebSocket session from which the unrecognized message was received
     */
    private void handleUnrecognizedMessage(Session session)
    {
        logger.error("Received unrecognized message");
        session.close(StatusCode.PROTOCOL, "Received unrecognized message, that does not conform to the MMTP specification");
    }
}
