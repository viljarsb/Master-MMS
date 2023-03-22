package MMS.Agent.WebSocketEndpointManager;

import MMS.Agent.AgentCallback;

import MMS.MMTP.ApplicationMessage;
import MMS.MMTP.MessageType;
import MMS.MMTP.ProtocolMessageEnvelope;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageHandler
{
    private static final Logger logger = LogManager.getLogger(MessageHandler.class);

    private final ExecutorService workerPool;
    private final AgentCallback agentCallback;


    /**
     * Initializes a MessageHandler with the provided callback.
     *
     * @param callback The message callback that will be used for processing messages.
     */
    public MessageHandler(AgentCallback callback)
    {
        this.workerPool = Executors.newCachedThreadPool();
        this.agentCallback = callback;
    }


    /**
     * Handles the incoming binary message by submitting it for parsing and processing.
     *
     * @param message The received binary message.
     */
    public void handle(byte[] message)
    {
        workerPool.submit(() ->
        {
            try
            {
                parseMessage(message);
            }

            catch (InvalidProtocolBufferException ex)
            {
                logger.error("Failed to parse message", ex);
            }
        });
    }


    /**
     * Parses the binary message into a ProtocolMessage and processes the payload.
     *
     * @param message The binary message to be parsed.
     * @throws InvalidProtocolBufferException If the message parsing fails or the payload is invalid.
     */
    private void parseMessage(byte[] message) throws InvalidProtocolBufferException
    {
        ProtocolMessageEnvelope protocolMessage = ProtocolMessageEnvelope.parseFrom(message);

        if (protocolMessage.getType() == MessageType.APPLICATION_MESSAGE)
        {
            processApplicationMessage(protocolMessage);
        }

        else
        {
            logger.error("Unknown message type received");
        }
    }


    /**
     * Processes an application message by calling the appropriate method on the message callback.
     *
     * @param applicationMessage The application message to be processed.
     */
    private void processApplicationMessage(ProtocolMessageEnvelope protocolMessage) throws InvalidProtocolBufferException
    {
        ApplicationMessage applicationMessage = ApplicationMessage.parseFrom(protocolMessage.getContent());

        byte[] payload = applicationMessage.getPayload().toByteArray();
        Instant expires = Instant.ofEpochSecond(applicationMessage.getExpires());
        String sender = applicationMessage.getSender();
        String messageId = applicationMessage.getId();

        if (applicationMessage.hasSubject())
        {
            String subject = applicationMessage.getSubject();
            agentCallback.onSubjectCast(subject, payload, expires, sender, messageId);
        }

        else if (applicationMessage.hasRecipient())
        {
            agentCallback.onDirectMessage(payload, expires, sender, messageId);
        }

        else
        {
            logger.error("Application message has neither subject nor recipient");
        }
    }

}
