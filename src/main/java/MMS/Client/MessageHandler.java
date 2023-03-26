package MMS.Client;

import MMS.Client.Exceptions.MMTPValidationException;
import MMS.Client.Interfaces.MessageListener;
import MMS.Protocols.MMTP.MessageFormats.DirectApplicationMessage;
import MMS.Protocols.MMTP.MessageFormats.MessageType;
import MMS.Protocols.MMTP.MessageFormats.ProtocolMessage;
import MMS.Protocols.MMTP.MessageFormats.SubjectCastApplicationMessage;
import MMS.Protocols.MMTP.Validators.MMTPValidator;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * MessageHandler is responsible for processing incoming messages and
 * notifying the appropriate MessageListener. It uses a thread pool
 * to process messages in parallel.
 */
public class MessageHandler
{
    private final static Logger logger = LogManager.getLogger(MessageHandler.class);

    private final MessageListener messageListener;
    private final ExecutorService workerPool;


    /**
     * Constructs a new MessageHandler with the specified MessageListener.
     *
     * @param messageListener the listener for handling message events
     */
    public MessageHandler(MessageListener messageListener)
    {
        this.messageListener = messageListener;
        int cores = Runtime.getRuntime().availableProcessors();
        workerPool = Executors.newFixedThreadPool(cores);
    }


    /**
     * Processes the given message payload using a worker thread from the thread pool.
     *
     * @param payload the message payload
     * @param offset  the starting position of the payload
     * @param len     the length of the payload
     */
    public void processMessage(byte[] payload, int offset, int len)
    {
        delegate(() -> processMessageInternal(payload, offset, len));
    }


    /**
     * Processes the given message payload internally by parsing the message
     * and calling the appropriate method based on the message type.
     *
     * @param payload the message payload
     * @param offset  the starting position of the payload
     * @param len     the length of the payload
     */
    private void processMessageInternal(byte[] payload, int offset, int len)
    {
        byte[] message = new byte[len];
        System.arraycopy(payload, offset, message, 0, len);

        try
        {
            ProtocolMessage protocolMessage = ProtocolMessage.parseFrom(message);
            MessageType type = protocolMessage.getType();

            switch (type)
            {
                case DIRECT_APPLICATION_MESSAGE ->
                {
                    DirectApplicationMessage directApplicationMessage = DirectApplicationMessage.parseFrom(protocolMessage.getContent());
                    processMessage(directApplicationMessage);
                }

                case SUBJECT_CAST_APPLICATION_MESSAGE ->
                {
                    SubjectCastApplicationMessage subjectCastApplicationMessage = SubjectCastApplicationMessage.parseFrom(protocolMessage.getContent());
                    processMessage(subjectCastApplicationMessage);
                }

                default -> logger.error("Unknown message type: " + type);
            }
        }

        catch (InvalidProtocolBufferException ex)
        {
            logger.error("Invalid protocol buffer", ex);
        }
    }


    /**
     * Processes a DirectApplicationMessage by validating it and notifying the MessageListener.
     *
     * @param message the DirectApplicationMessage to be processed
     */
    private void processMessage(DirectApplicationMessage message)
    {
        try
        {
            MMTPValidator.validate(message);
        }

        catch (MMTPValidationException ex)
        {
            logger.error("The application message is not valid, dropping it");
            return;
        }

        String messageId = message.getId();
        List<String> destinations = message.getRecipientsList();
        String sender = message.getSender();
        Instant expires = Instant.ofEpochSecond(message.getExpires().getSeconds(), message.getExpires().getNanos());
        byte[] content = message.getPayload().toByteArray();

        messageListener.onDirectMessage(messageId, destinations, sender, expires, content);
    }


    /**
     * Processes a SubjectCastApplicationMessage by validating it and notifying the MessageListener.
     *
     * @param message the SubjectCastApplicationMessage to be processed
     */
    private void processMessage(SubjectCastApplicationMessage message)
    {
        try
        {
            MMTPValidator.validate(message);
        }

        catch (MMTPValidationException ex)
        {
            logger.error("The application message is not valid, dropping it");
            return;
        }

        String messageId = message.getId();
        String sender = message.getSender();
        Instant expires = Instant.ofEpochSecond(message.getExpires().getSeconds(), message.getExpires().getNanos());
        String subject = message.getSubject();
        byte[] content = message.getPayload().toByteArray();

        messageListener.onSubjectCastMessage(messageId, sender, subject, expires, content);
    }


    /**
     * Delegates the processing of a message to a worker thread in the thread pool.
     *
     * @param runnable the task to be executed by a worker thread
     */
    private void delegate(Runnable runnable)
    {
        workerPool.execute(runnable);
    }
}
