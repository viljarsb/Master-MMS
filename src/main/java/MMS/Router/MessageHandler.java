package MMS.Router;

import MMS.MMTP.ApplicationMessage;
import MMS.MMTP.ProtocolMessageEnvelope;
import MMS.MMTP.Subscribe;
import MMS.MMTP.Unsubscribe;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageHandler
{
    private final static Logger logger = LogManager.getLogger(MessageHandler.class);

    private final static MessageHandler instance = new MessageHandler();
    private final ExecutorService workerPool;
    private final MessageBroker messageBroker;


    private MessageHandler()
    {
        workerPool = Executors.newCachedThreadPool();
        messageBroker = MessageBroker.getBroker();
        logger.info("Message handler started");
    }


    public static MessageHandler getHandler()
    {
        return instance;
    }


    public void handle(byte[] message)
    {
        workerPool.execute(() ->
        {
            logger.info("Message handler received message: {}", message);

            try
            {
                ProtocolMessageEnvelope envelope = ProtocolMessageEnvelope.parseFrom(message);

                switch (envelope.getType())
                {
                    case APPLICATION_MESSAGE -> handleApplicationMessage(envelope.getContent().toByteArray());
                    case SUBSCRIBE -> handleSubscribeMessage(envelope.getContent().toByteArray());
                    case UNSUBSCRIBE -> handleUnsubscribeMessage(envelope.getContent().toByteArray());
                }
            }
            catch (InvalidProtocolBufferException ex)
            {
                logger.error("Error parsing message", ex);
            }
        });
    }


    private void handleApplicationMessage(byte[] message) throws InvalidProtocolBufferException
    {
        ApplicationMessage applicationMessage = ApplicationMessage.parseFrom(message);
        if(!isMessageValid(applicationMessage))
        {
            logger.error("Invalid message received");
            return;
        }

        messageBroker.routeMessage(applicationMessage);
    }


    private void handleSubscribeMessage(byte[] message) throws InvalidProtocolBufferException
    {
        Subscribe subscribe = Subscribe.parseFrom(message);
        if(!isMessageValid(subscribe))
        {
            logger.error("Invalid message received");
            return;
        }
    }


    private void handleUnsubscribeMessage(byte[] message) throws InvalidProtocolBufferException
    {
        Unsubscribe unsubscribe = Unsubscribe.parseFrom(message);
        if(!isMessageValid(unsubscribe))
        {
            logger.error("Invalid message received");
            return;
        }
    }

    private boolean isMessageValid(ApplicationMessage message)
    {
        return true;
    }

    private boolean isMessageValid(Subscribe message)
    {
        return true;
    }

    private boolean isMessageValid(Unsubscribe message)
    {
        return true;
    }
}
