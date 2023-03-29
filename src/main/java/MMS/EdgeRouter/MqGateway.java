package MMS.EdgeRouter;

import MMS.Misc.MrnValidator;
import MMS.Misc.SubjectValidator;
import MMS.Protocols.MMTP.MessageFormats.DirectApplicationMessage;
import MMS.Protocols.MMTP.MessageFormats.SubjectCastApplicationMessage;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.*;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * This class represents an MQ gateway that allows for sending and receiving messages to/from an MQ broker. It implements
 * the RemoteConnection interface for external use, and also implements the MessageListener and ExceptionListener
 * interfaces for receiving messages and handling exceptions, respectively.
 * <p>
 * This is the normal way to connect to the "MMS network" for this testbed, this is not an endorsed solution.
 */
public class MqGateway implements RemoteConnection, MessageListener, ExceptionListener
{
    private static final Logger logger = LogManager.getLogger(MqGateway.class);
    private static final int RECONNECT_DELAY_MS = 60_000;

    private static MqGateway instance;

    private final ConnectionListener connectionListener;
    private final String brokerURL;

    private Connection connection;
    private Session session;
    private final ConcurrentHashMap<String, MessageConsumer> messageConsumers = new ConcurrentHashMap<>();


    /**
     * Constructor for MqGateway. This should not be called directly, use the create method instead.
     * Once created the gateway is a self-contained object that will attempt to maintain a connection to the MQ broker.
     * It will inform the connection listener of any connection status changes (the forwarding engine).
     *
     * @param brokerURL          the URL for the MQ broker
     * @param connectionListener listener for connection status changes
     */
    private MqGateway(String brokerURL, ConnectionListener connectionListener)
    {
        this.brokerURL = brokerURL;
        this.connectionListener = connectionListener;
    }


    /**
     * Creates a new instance of MqGateway if one does not already exist, and establishes a connection to the MQ broker.
     *
     * @param brokerURL          the URL for the MQ broker
     * @param connectionListener listener for connection status changes
     * @return the singleton instance of MqGateway
     */
    public static synchronized MqGateway create(String brokerURL, ConnectionListener connectionListener)
    {
        if (instance == null)
        {
            instance = new MqGateway(brokerURL, connectionListener);
            instance.establishConnection();
        }
        return instance;
    }


    /**
     * Establishes a connection to the MQ broker.
     */
    private void establishConnection()
    {
        if (connection != null || session != null)
        {
            return;
        }

        try
        {
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerURL);
            connection = connectionFactory.createConnection();
            connection.start();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            connectionListener.onConnection(this, new ConnectionStatus(10.0, 10.0)); // random numbers for now.

            connection.setExceptionListener(this);
        }

        catch (JMSException ex)
        {
            logger.error("There was an error connecting to MQ broker: {}", brokerURL, ex);
            attemptReconnect();
        }
    }


    /**
     * Attempts to reconnect to the MQ broker after a delay time of RECONNECT_DELAY_MS.
     */
    private void attemptReconnect()
    {
        ThreadPoolService.executeAsync(() ->
        {
            try
            {
                logger.info("Going to sleep for {} seconds before attempting to reconnect to MQ broker: {}", RECONNECT_DELAY_MS / 1000, brokerURL);
                Thread.sleep(RECONNECT_DELAY_MS);
            }
            catch (InterruptedException ex)
            {
                logger.error("Thread interrupted while waiting to reconnect to MQ broker: {}", brokerURL, ex);
            }

            establishConnection();
        }, TaskPriority.MEDIUM);
    }


    /**
     * Closes the connection to the MQ broker.
     */
    private void closeConnection()
    {
        try
        {
            for (MessageConsumer consumer : messageConsumers.values())
            {
                try
                {
                    consumer.close();
                }

                catch (JMSException ex)
                {
                    logger.debug("Error closing consumer", ex);
                }
            }

            session.close();
            session = null;
            connection.close();
            connection = null;

            connectionListener.onDisconnection(this);
        }

        catch (JMSException ex)
        {
            logger.debug("Error occurred when closing connections to MQ Broker", ex);
        }
    }


    /**
     * Handles exceptions that occur with the MQ connection by logging the error, closing the connection, and attempting
     * to reconnect.
     *
     * @param ex the JMSException that occurred
     */
    private void handleException(JMSException ex)
    {
        logger.error("Error occurred with MQ connection", ex);
        closeConnection();
        attemptReconnect();
    }


    /**
     * Subscribes to the given subjects and MRNs with the remote connection.
     * This effectively tells the remote connection that the edge router is interested
     * in receiving messages for the given subjects and MRNs. Since MRNs may be active
     * in many locations at once, they are treated as subjects, but the edge router
     * should not allow agents to subscribe to MRNs they are not authenticated as.
     *
     * @param subjects the set of subjects to subscribe to
     * @param MRNs     the set of MRNs to subscribe to
     */
    private void subscribe(Set<String> subjects, Set<String> MRNs)
    {
        if (subjects != null)
        {
            for (String subject : subjects)
            {
                if (SubjectValidator.validate(subject))
                {
                    createAndRegisterConsumer(subject);
                }
            }
        }

        if (MRNs != null)
        {
            for (String mrn : MRNs)
            {
                if (MrnValidator.validate(mrn) && !messageConsumers.containsKey(mrn))
                {
                    createAndRegisterConsumer(mrn);
                }
            }
        }
    }


    /**
     * Creates and registers a message consumer for the given identifier, if one does not already exist.
     *
     * @param identifier the identifier for the message consumer
     */
    private void createAndRegisterConsumer(String identifier)
    {
        if (messageConsumers.containsKey(identifier))
        {
            return;
        }

        Destination destination = createDestination(identifier);
        if (destination == null)
        {
            return;
        }

        MessageConsumer consumer = createMessageConsumer(destination);
        if (consumer == null)
        {
            return;
        }

        if (setMessageListener(consumer))
        {
            messageConsumers.put(identifier, consumer);
        }
    }


    /**
     * Creates a destination for the given identifier.
     *
     * @param identifier the identifier for the destination
     * @return the created destination, or null if an error occurred
     */
    private Destination createDestination(String identifier)
    {
        try
        {
            return session.createTopic(identifier);
        }

        catch (JMSException ex)
        {
            logger.error("Could not create destination for identifier: {}", identifier, ex);
            return null;
        }
    }


    /**
     * Creates a message consumer for the given destination.
     *
     * @param destination the destination to create the consumer for
     * @return the created message consumer, or null if an error occurred
     */
    private MessageConsumer createMessageConsumer(Destination destination)
    {
        try
        {
            return session.createConsumer(destination, null, true);
        }

        catch (JMSException ex)
        {
            logger.error("Could not create consumer for destination: {}", destination.toString(), ex);
            return null;
        }
    }


    /**
     * Sets the message listener for the given message consumer.
     * In this case, the message listener is the RemoteConnection itself.
     * This allows the RemoteConnection to receive messages from the MQ broker
     * for the subjects and MRNs it is subscribed to.
     *
     * @param consumer the message consumer to set the listener for
     * @return true if the listener was set successfully, false otherwise
     */
    private boolean setMessageListener(MessageConsumer consumer)
    {
        try
        {
            consumer.setMessageListener(this);
            return true;
        }
        catch (JMSException ex)
        {
            logger.error("An error occurred with setting up message listener.", ex);
            return false;
        }
    }


    /**
     * Unsubscribes from the given subjects and MRNs with the remote connection.
     * This effectively tells the remote connection that the edge router is no longer
     * interested in receiving messages for the given subjects and MRNs.
     *
     * @param subjects the set of subjects to unsubscribe from
     * @param MRNs     the set of MRNs to unsubscribe from
     */
    private void unsubscribe(Set<String> subjects, Set<String> MRNs)
    {
        Set<String> combinedIdentifiers = new HashSet<>();

        if (subjects != null)
        {
            combinedIdentifiers.addAll(subjects);
        }

        if (MRNs != null)
        {
            combinedIdentifiers.addAll(MRNs);
        }

        combinedIdentifiers.forEach(this::closeConsumer);
    }


    /**
     * Closes the message consumer for the given identifier.
     * Called once the edge router is unsubscribed from the given identifier.
     *
     * @param identifier the identifier for the message consumer to close
     */
    private void closeConsumer(String identifier)
    {
        MessageConsumer consumer = messageConsumers.get(identifier);
        if (consumer == null)
        {
            return;
        }

        try
        {
            consumer.close();
            messageConsumers.remove(identifier);
        }

        catch (JMSException ex)
        {
            logger.error("Problem closing MQ message consumer for identifier: {}", identifier, ex);
        }
    }


    /**
     * Sends the given Protocol Buffers message to the MQ broker by creating and sending a BytesMessage to the
     * appropriate topic. Destinations are determined by the type of message being sent.
     *
     * @param message the message to send
     */
    private void sendToBroker(com.google.protobuf.Message message)
    {
        Set<String> destinations = new HashSet<>();

        if (message instanceof SubjectCastApplicationMessage)
        {
            String subject = ((SubjectCastApplicationMessage) message).getSubject();
            destinations.add(subject);
        }

        else if (message instanceof DirectApplicationMessage)
        {
            List<String> recipients = ((DirectApplicationMessage) message).getRecipientsList();
            destinations.addAll(recipients);
        }

        else
        {
            logger.error("Unknown message type: " + message.getClass().getName());
            return;
        }

        //ideally we want to use composite destinations here, fix in v2.
        for (String current : destinations)
        {
            try
            {
                Destination dest = session.createTopic(current);
                MessageProducer producer = session.createProducer(dest);
                BytesMessage bytesMessage = session.createBytesMessage();
                bytesMessage.writeBytes(message.toByteArray());
                producer.send(bytesMessage);
            }

            catch (JMSException ex)
            {
                logger.debug("Error occurred with sending to MQ", ex);
            }
        }
    }


    /**
     * Handles an incoming remote message by extracting the message data from the BytesMessage and passing it to the
     * ConnectionListener (MessageForwarder) for processing.
     *
     * @param message the incoming message
     */
    private void handleIncomingRemoteMessage(Message message)
    {
        try
        {
            logger.debug("Received message from MQ, topic: {}", message.getJMSDestination().toString());
            BytesMessage bytesMessage = (BytesMessage) message;
            byte[] bytes = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(bytes);
            connectionListener.onMessage(this, ByteBuffer.wrap(bytes));
        }

        catch (JMSException ex)
        {
            logger.debug("Error with message retrieval from MQ", ex);
        }
    }


    /**
     * Called by the MessageForwarder when a message is to be sent to the remote connection.
     *
     * @param message the message to send
     */
    @Override
    public void send(com.google.protobuf.Message message)
    {
        ThreadPoolService.executeAsync(() -> sendToBroker(message), TaskPriority.HIGH);
    }


    /**
     * Called by the MessageForwarder when a new subscription is added.
     *
     * @param subjects the set of subjects to subscribe to
     * @param MRNs     the set of MRNs to subscribe to
     */
    @Override
    public void addSubscriptions(Set<String> subjects, Set<String> MRNs)
    {
        ThreadPoolService.executeAsync(() -> subscribe(subjects, MRNs), TaskPriority.HIGH);
    }


    /**
     * Called by the MessageForwarder when a subscription is removed.
     *
     * @param subjects the set of subjects to unsubscribe from
     * @param MRNs     the set of MRNs to unsubscribe from
     */
    @Override
    public void removeSubscriptions(Set<String> subjects, Set<String> MRNs)
    {
        ThreadPoolService.executeAsync(() -> unsubscribe(subjects, MRNs), TaskPriority.HIGH);
    }


    /**
     * Called by the MessageForwarder when the connection is to be shutdown.
     * Typically called when the edge router is shutting down.
     */
    @Override
    public void shutdown()
    {
        ThreadPoolService.executeAsync(this::closeConnection, TaskPriority.HIGH);
    }


    /**
     * Called by the MessageConsumers when a message is received for some subject or MRN.
     *
     * @param message the message received
     */
    @Override
    public void onMessage(Message message)
    {
        ThreadPoolService.executeAsync(() -> handleIncomingRemoteMessage(message), TaskPriority.HIGH);
    }


    /**
     * Called by the underlying MQ connection when an exception occurs.
     *
     * @param exception the exception that occurred
     */
    @Override
    public void onException(JMSException exception)
    {
        ThreadPoolService.executeAsync(() -> handleException(exception), TaskPriority.HIGH);
    }
}
