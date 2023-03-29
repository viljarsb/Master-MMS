package MMS.EdgeRouter;


import MMS.Protocols.MMTP.MessageFormats.*;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


/**
 * The MessageForwardingEngine class is responsible for forwarding messages locally and
 * to and from remote gateways, it should inform the remote gateways of new subscriptions
 * or changes to existing subscriptions.
 */
public class MessageForwardingEngine implements ConnectionListener
{
    private static final Logger logger = LogManager.getLogger(MessageForwardingEngine.class);
    private static MessageForwardingEngine instance;

    private static final SubscriptionManager subscriptionManager = SubscriptionManager.getInstance();
    private final List<ConnectionContainer> remoteGateways = new CopyOnWriteArrayList<>();


    /**
     * Initializes a new instance of the MessageForwardingEngine class.
     */
    private MessageForwardingEngine()
    {
        initRemoteGateways();
        logger.info("Message Forwarding Engine Initialized");
    }


    /**
     * Returns the singleton instance of the MessageForwardingEngine.
     *
     * @return The singleton instance of the MessageForwardingEngine.
     */
    public synchronized static MessageForwardingEngine getForwarder()
    {
        if (instance == null)
        {
            instance = new MessageForwardingEngine();
        }

        return instance;
    }


    /**
     * Initializes the remote gateways, this is mostly for testing purposes.
     */
    private void initRemoteGateways()
    {
        MqGateway.create("tcp://localhost:61616", this);
    }


    /**
     * Forwards a SubjectCastApplicationMessage to both local and remote subscribers.
     *
     * @param message The SubjectCastApplicationMessage to forward.
     */
    public void forwardMessage(SubjectCastApplicationMessage message)
    {
        ThreadPoolService.executeAsync(() ->
        {
            forwardMessageLocally(message);
            forwardRemote(message);
        }, TaskPriority.CRITICAL);
    }


    /**
     * Forwards a DirectApplicationMessage to both local and remote destinations.
     *
     * @param message The DirectApplicationMessage to forward.
     */
    public void forwardMessage(DirectApplicationMessage message)
    {
        ThreadPoolService.executeAsync(() ->
        {
            forwardMessageLocally(message);
            forwardRemote(message);
        }, TaskPriority.CRITICAL);
    }


    /**
     * Forwards a message to a remote gateway.
     *
     * @param message The message to forward.
     */
    private void forwardRemote(Message message)
    {
        RemoteConnection connection = findOptimalLink();

        if (connection != null)
        {
            if (message instanceof SubjectCastApplicationMessage)
                connection.send(message);
            else if (message instanceof DirectApplicationMessage)
                connection.send(message);
            else
                logger.debug("Invalid message, ignore");
        }
    }


    /**
     * Forwards a SubjectCastApplicationMessage to local subscribers.
     *
     * @param message The SubjectCastApplicationMessage to forward.
     */
    private void forwardMessageLocally(SubjectCastApplicationMessage message)
    {
        ThreadPoolService.executeAsync(() ->
        {
            String subject = message.getSubject();
            MessageType type = MessageType.SUBJECT_CAST_APPLICATION_MESSAGE;
            ByteString messageBytes = message.toByteString();

            ProtocolMessage protocolMessage = ProtocolMessage.newBuilder()
                    .setType(type)
                    .setContent(messageBytes)
                    .build();

            ByteBuffer buffer = ByteBuffer.wrap(protocolMessage.toByteArray());

            subscriptionManager.getSubscribers(subject)
                    .forEach(session -> session.getRemote()
                            .sendBytesByFuture(buffer));
        }, TaskPriority.HIGH);
    }


    /**
     * Forwards a DirectApplicationMessage to local subscribers.
     *
     * @param message The DirectApplicationMessage to forward.
     */
    private void forwardMessageLocally(DirectApplicationMessage message)
    {
        ThreadPoolService.executeAsync(() ->
        {
            List<String> destinations = message.getRecipientsList();
            List<Session> sessions = new ArrayList<>();

            for (String destination : destinations)
            {
                sessions.addAll(subscriptionManager.getMRNSubscribers(destination));
            }

            MessageType type = MessageType.DIRECT_APPLICATION_MESSAGE;
            ByteString messageBytes = message.toByteString();

            ProtocolMessage protocolMessage = ProtocolMessage.newBuilder()
                    .setType(type)
                    .setContent(messageBytes)
                    .build();

            ByteBuffer buffer = ByteBuffer.wrap(protocolMessage.toByteArray());
            sessions.forEach(session -> session.getRemote().sendBytesByFuture(buffer));
        }, TaskPriority.HIGH);
    }


    /**
     * Finds the optimal remote connection to forward a message to.
     *
     * @return The optimal remote connection, or null if no connections are available.
     */
    private RemoteConnection findOptimalLink()
    {
        RemoteConnection optimalConnection = null;
        double maxEffectiveBandwidth = 0.0;

        for (ConnectionContainer connection : remoteGateways)
        {
            double expectedBandwidth = connection.getStatus().getExpectedBandwidth();
            double expectedDelay = connection.getStatus().getExpectedDelay();
            double effectiveBandwidth = expectedBandwidth / (1 + expectedDelay);

            if (effectiveBandwidth > maxEffectiveBandwidth)
            {
                maxEffectiveBandwidth = effectiveBandwidth;
                optimalConnection = connection.getConnection();
            }
        }

        return optimalConnection;
    }


    /**
     * Parses incoming messages received from remote gateways.
     *
     * @param buffer The buffer containing the incoming message.
     */
    private void handleIncomingRemoteMessage(ByteBuffer buffer)
    {
        try
        {
            ProtocolMessage message = ProtocolMessage.parseFrom(buffer);
            MessageType type = message.getType();

            switch (type)
            {
                case DIRECT_APPLICATION_MESSAGE, SUBJECT_CAST_APPLICATION_MESSAGE -> parseIncomingMessage(message);
            }
        }

        catch (InvalidProtocolBufferException e)
        {
            logger.debug("Message Invalid, dropping.");
        }
    }


    /**
     * Parses an incoming message and forwards it locally.
     *
     * @param message The ProtocolMessage to parse.
     * @throws InvalidProtocolBufferException If the ProtocolMessage is invalid.
     */
    private void parseIncomingMessage(ProtocolMessage message) throws InvalidProtocolBufferException
    {
        MessageType type = message.getType();

        if (type == MessageType.SUBJECT_CAST_APPLICATION_MESSAGE)
        {
            SubjectCastApplicationMessage appMsg = SubjectCastApplicationMessage.parseFrom(message.getContent());
            forwardMessageLocally(appMsg);
        }

        else if (type == MessageType.DIRECT_APPLICATION_MESSAGE)
        {
            DirectApplicationMessage appMsg = DirectApplicationMessage.parseFrom(message.getContent());
            forwardMessageLocally(appMsg);
        }

        else
        {
            logger.debug("Invalid message, ignore");
        }
    }


    /**
     * Checks if any remote connections are available.
     *
     * @return True if at least one remote connection is available, false otherwise.
     */
    public boolean isRemoteConnectionAvailable()
    {
        return !remoteGateways.isEmpty();
    }


    /**
     * Adds subscriptions for the specified subjects and MRNs to all remote connections.
     *
     * @param subjects The subjects to subscribe to.
     * @param MRNs     The MRNs to subscribe to.
     */
    public void addSubscription(Set<String> subjects, Set<String> MRNs)
    {
        for (ConnectionContainer connection : remoteGateways)
        {
            connection.getConnection().addSubscriptions(subjects, MRNs);
        }
    }


    /**
     * Removes subscriptions for the specified subjects and MRNs from all remote connections.
     *
     * @param subjects The subjects to unsubscribe from.
     * @param MRNs     The MRNs to unsubscribe from.
     */
    public void removeSubscription(Set<String> subjects, Set<String> MRNs)
    {
        for (ConnectionContainer connection : remoteGateways)
        {
            connection.getConnection().removeSubscriptions(subjects, MRNs);
        }
    }


    /**
     * Handles a new connection event (a new remote gateway).
     *
     * @param connection The new RemoteConnection object.
     * @param status     The ConnectionStatus object representing the connection status.
     */
    @Override
    public void onConnection(RemoteConnection connection, ConnectionStatus status)
    {
        ThreadPoolService.executeAsync(() ->
        {
            logger.info("A new connection was established");
            ConnectionContainer connectionContainer = new ConnectionContainer(connection, status);
            remoteGateways.add(connectionContainer);
            Set<String> subjects = subscriptionManager.getAllMRNs();
            Set<String> MRNs = subscriptionManager.getAllSubjects();
            connection.addSubscriptions(subjects, MRNs);
        }, TaskPriority.CRITICAL);
    }


    /**
     * Handles a disconnection event (loss of a remote gateway).
     *
     * @param connection The RemoteConnection object that was disconnected.
     */
    @Override
    public void onDisconnection(RemoteConnection connection)
    {
        ThreadPoolService.executeAsync(() ->
        {
            logger.info("A remote connection was lost");
            remoteGateways.removeIf(remoteConnection -> remoteConnection.getConnection() == connection);
        }, TaskPriority.CRITICAL);
    }


    /**
     * Handles a connection condition change event. This is called by remote gateways to notify
     * the edge router of a change in the connection status, such as heavy increase in latency.
     * This should be extended in the future to handle other types of connection conditions.
     *
     * @param connection The RemoteConnection object for which the condition changed.
     * @param status     The new ConnectionStatus object representing the changed condition.
     */
    @Override
    public void onConditionChange(RemoteConnection connection, ConnectionStatus status)
    {
        ThreadPoolService.executeAsync(() ->
        {
            logger.info("A remote connection condition changed.");
            for (ConnectionContainer remoteConnection : remoteGateways)
            {
                if (remoteConnection.getConnection() == connection)
                {
                    remoteConnection.updateStatus(status);
                }
            }
        }, TaskPriority.CRITICAL);
    }


    /**
     * Handles an incoming message event from a remote gateway.
     *
     * @param connection The RemoteConnection object from which the message was received.
     * @param message    The ByteBuffer containing the incoming message.
     */
    @Override
    public void onMessage(RemoteConnection connection, ByteBuffer message)
    {
        ThreadPoolService.executeAsync(() ->
        {
            logger.debug("Received a message from a remote gateway");
            handleIncomingRemoteMessage(message);
        }, TaskPriority.HIGH);
    }


    /**
     * Shutdown the remote gateway connections, should be called before shutting down the edge router.
     */
    public void shutdown()
    {
        logger.info("Shutting down remote gateway connections");
        List<Future<?>> tasks = new ArrayList<>();
        for (ConnectionContainer connection : remoteGateways)
        {
            RemoteConnection remoteConnection = connection.getConnection();
            tasks.add(ThreadPoolService.executeAsyncWithResult(remoteConnection::shutdown, TaskPriority.CRITICAL));
        }

        for (Future<?> future : tasks)
        {
            try
            {
                future.get(3, TimeUnit.SECONDS);
                logger.info("A remote gateway connection was successfully shut down");
            }

            catch (Exception ignored) {}
        }
    }


    /**
     * A private class used to encapsulate RemoteConnection objects and their associated ConnectionStatus objects.
     */
    private static class ConnectionContainer
    {
        private final RemoteConnection connection;
        private ConnectionStatus status;


        /**
         * Initializes a new instance of the ConnectionContainer class.
         * This class is used to encapsulate RemoteConnection objects and their associated ConnectionStatus objects.
         *
         * @param connection The RemoteConnection object to encapsulate.
         * @param status     The ConnectionStatus object associated with the RemoteConnection object.
         */
        public ConnectionContainer(RemoteConnection connection, ConnectionStatus status)
        {
            this.connection = connection;
            this.status = status;
        }


        public RemoteConnection getConnection()
        {
            return connection;
        }


        public ConnectionStatus getStatus()
        {
            return status;
        }


        public void updateStatus(ConnectionStatus status)
        {
            this.status = status;
        }
    }
}