package MMS.EdgeRouter.WebsocketServerManager;

import MMS.EdgeRouter.Config;
import MMS.EdgeRouter.SubscriptionManager.SubscriptionManager;
import MMS.MMTP.*;
import MMS.Misc.MrnValidator;
import com.google.protobuf.InvalidProtocolBufferException;
import net.maritimeconnectivity.pki.PKIIdentity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageHandler
{
    private static final Logger logger = LogManager.getLogger(MessageHandler.class);
    private static MessageHandler instance;

    private final ExecutorService workerService;
    private final SubscriptionManager subscriptionManager;


    private MessageHandler()
    {
        this.workerService = Executors.newFixedThreadPool(Config.getNumOfWorkerThreads());
        this.subscriptionManager = SubscriptionManager.getManager();
    }


    public static MessageHandler getHandler()
    {
        if (instance == null)
        {
            instance = new MessageHandler();
            logger.info("MessageHandler created");
        }
        return instance;
    }


    public void handleMessage(byte[] payload, int offset, int len, String connectionID)
    {
        logger.info("Binary message received, handing off to worker thread");
        workerService.execute(() -> processBinaryMessage(payload, offset, len, connectionID));

    }


    private void processBinaryMessage(byte[] payload, int offset, int len, String connectionID)
    {
        byte[] message = new byte[len];
        System.arraycopy(payload, offset, message, 0, len);

        try
        {
            ProtocolMessageEnvelope protocolMessage = ProtocolMessageEnvelope.parseFrom(message);

            switch (protocolMessage.getType())
            {
                case APPLICATION_MESSAGE -> processApplicationMessage(protocolMessage, connectionID);
                case SUBSCRIBE -> processSubscribeMessage(protocolMessage, connectionID);
                case UNSUBSCRIBE -> processUnsubscribeMessage(protocolMessage, connectionID);
                case DIRECT_MESSAGE_PREFERENCE -> processDirectMessagePreference(protocolMessage, connectionID);
                default -> logger.error("Unknown message type received"); // cant happen
            }
        }

        catch (InvalidProtocolBufferException ex)
        {
            logger.error("Error parsing binary message: " + ex.getMessage());
        }
    }


    private void processApplicationMessage(ProtocolMessageEnvelope protocolMessage, String connectionID)
    {
        try
        {
            ApplicationMessage applicationMessage = ApplicationMessage.parseFrom(protocolMessage.getContent());
            ConnectionHandler connectionHandler = ConnectionHandler.getHandler();
            ConnectionState state = connectionHandler.getConnectionState(connectionID);

            PKIIdentity identity = state.getIdentity();

            if (identity == null)
            {
                logger.error("An agent tried to send a message without being authenticated");
                return;
            }

            boolean valid = validateMessage(applicationMessage, identity);

            if (!valid)
            {
                logger.error("Message validation failed");
                return;
            }

            //TODO handle message
        }

        catch (InvalidProtocolBufferException ex)
        {
            logger.error("Error parsing application message: " + ex.getMessage());
        }
    }


    private void processSubscribeMessage(ProtocolMessageEnvelope protocolMessage, String connectionID) throws InvalidProtocolBufferException
    {
        try
        {
            Subscribe subscribe = Subscribe.parseFrom(protocolMessage.getContent());


            boolean valid = validateMessage(subscribe.getInterestsList());

            if (!valid)
            {
                logger.error("Message validation failed");
                return;
            }

            subscriptionManager.subscribe(subscribe.getInterestsList(), connectionID);
        }

        catch (InvalidProtocolBufferException ex)
        {
            logger.error("Error parsing subscribe message: " + ex.getMessage());
        }
    }


    private void processUnsubscribeMessage(ProtocolMessageEnvelope protocolMessage, String connectionID) throws InvalidProtocolBufferException
    {
        try
        {
            Unsubscribe unsubscribe = Unsubscribe.parseFrom(protocolMessage.getContent());


            boolean valid = validateMessage(unsubscribe.getInterestsList());

            if (!valid)
            {
                logger.error("Message validation failed");
                return;
            }

            subscriptionManager.unsubscribe(unsubscribe.getInterestsList(), connectionID);
        }

        catch (InvalidProtocolBufferException ex)
        {
            logger.error("Error parsing subscribe message: " + ex.getMessage());
        }
    }


    private void processDirectMessagePreference(ProtocolMessageEnvelope protocolMessage, String connectionID) throws InvalidProtocolBufferException
    {
        try
        {
            DirectMessagePreference directMessagePreference = DirectMessagePreference.parseFrom(protocolMessage.getContent());

            boolean wantsDirectMessages = directMessagePreference.getWantDirectMessages();

            PKIIdentity identity = ConnectionHandler.getHandler().getConnectionState(connectionID).getIdentity();

            if (identity == null)
            {
                logger.error("The agent tried to set direct message preference without being authenticated");
                return;
            }

            if (wantsDirectMessages)
                subscriptionManager.subscribeToDM(connectionID);

            else
                subscriptionManager.unsubscribeFromDM(connectionID);
        }

        catch (InvalidProtocolBufferException ex)
        {
            logger.error("Error parsing direct message preference message: " + ex.getMessage());
        }
    }


    private boolean validateMessage(ApplicationMessage message, PKIIdentity identity)
    {
        String messageID = message.getId();

        try
        {
            UUID uuid = UUID.fromString(messageID);
        }

        catch (IllegalArgumentException ex)
        {
            logger.error("The message ID is not a valid UUID");
            return false;
        }


        String connectedMRN = identity.getMrn();
        String messageMRN = message.getSender();

        if (!connectedMRN.equals(messageMRN))
        {
            logger.error("The connected MRN does not match the message sender MRN");
            return false;
        }

        if (message.hasRecipient())
        {
            String recipientMRN = message.getRecipient();
            if (!MrnValidator.validate(recipientMRN))
            {
                logger.error("The recipient MRN is not valid");
                return false;
            }
        }

        else if (message.hasSubject())
        {
            String subject = message.getSubject();
            if (subject.length() > 100 || subject.length() < 1)
            {
                logger.error("The subject is not valid");
                return false;
            }
        }

        long expirationTime = message.getExpires();
        Instant currentTime = Instant.now();
        Instant maxExpirationTime = currentTime.plus(Duration.ofDays(30));

        if (expirationTime > 0)
        {
            Instant expirationInstant = Instant.ofEpochSecond(expirationTime);
            if (expirationInstant.isAfter(maxExpirationTime))
            {
                logger.error("The message expiration time is too far in the future");
                return false;
            }

            if (expirationInstant.isBefore(currentTime))
            {
                logger.error("The message has already expired");
                return false;
            }
        }

        byte[] content = message.getPayload().toByteArray();

        if (content.length == 0)
        {
            logger.error("The message content is empty");
            return false;
        }

        return true;
    }


    private boolean validateMessage(List<String> subjects)
    {
        if (subjects.size() == 0)
        {
            logger.error("The subscribe message has no subjects");
            return false;
        }

        for (String subject : subjects)
        {
            if (subject.length() > 100 || subject.length() < 1)
            {
                logger.error("The subject is not valid");
                return false;
            }
        }

        return true;
    }

}

