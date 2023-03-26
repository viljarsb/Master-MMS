package MMS.Client;

import MMS.Client.Connections.*;
import MMS.Client.Exceptions.*;
import MMS.Client.Interfaces.*;
import MMS.Client.ServiceDiscovery.RouterInfo;
import MMS.Client.ServiceDiscovery.mDNSDiscoveryService;
import MMS.Client.TLSConfiguration.TLSConfig;
import MMS.Client.TLSConfiguration.mTLSConfig;
import MMS.Protocols.MMTP.MMTPUtils;
import MMS.Protocols.MMTP.MessageFormats.DirectApplicationMessage;
import MMS.Protocols.MMTP.MessageFormats.MessageType;
import MMS.Protocols.MMTP.MessageFormats.ProtocolMessage;
import MMS.Protocols.MMTP.MessageFormats.SubjectCastApplicationMessage;
import MMS.Protocols.MMTP.Validators.MMTPValidator;
import com.google.protobuf.ByteString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class Agent implements ConnectionListener, MessageListener, SubscribeListener
{
    private static final Logger logger = LogManager.getLogger(Agent.class);
    private static ExecutorService workerPool;
    private static mDNSDiscoveryService mDNSDiscoveryService;
    private static WsHandler wsHandler;

    private final SubscriptionManager subscriptionManager;
    private final AgentListener adapter;
    private Connection connection;
    private AgentStatus status;


    /**
     * Constructs a new Agent instance, with the specified AgentListener implementation.
     * Uses reflection to create a new instance of the AgentListener implementation,
     * not allowing the user to create an instance of the AgentListener interface.
     * This avoids the user to be able to supply the same instance of the AgentListener
     * interface to multiple Agent instances, this would cause problems.
     *
     * @param agentListenerImpl The implementation of the AgentListener interface.
     * @throws NoSuchMethodException     When the AgentListener implementation does not have a constructor with a ConnectionListener parameter.
     * @throws InvocationTargetException When the AgentListener implementation constructor throws an exception.
     * @throws InstantiationException    When the AgentListener implementation constructor throws an exception.
     * @throws IllegalAccessException    When the AgentListener implementation constructor throws an exception.
     */
    private Agent(Class<? extends AgentListener> agentListenerImpl) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException
    {
        this.adapter = agentListenerImpl.getDeclaredConstructor(ConnectionListener.class).newInstance(this);
        this.subscriptionManager = new SubscriptionManager(this);
        this.status = AgentStatus.NOT_CONNECTED;
        logger.info("A new agent instance was created");
    }


    /**
     * This method is used to get an instance of the Agent class.
     * It does initialize some static fields if they are null,
     * because these are shared between all instances of the Agent class.
     *
     * @param agentListenerImpl The implementation of the AgentListener interface.
     * @return Agent
     * @throws AgentInitException If the agent could not be initialized.
     */
    public static synchronized Agent getInstance(Class<? extends AgentListener> agentListenerImpl) throws AgentInitException
    {
        try
        {
            if (workerPool == null)
                workerPool = Executors.newCachedThreadPool();

            if (mDNSDiscoveryService == null)
                mDNSDiscoveryService = new mDNSDiscoveryService();

            if (wsHandler == null)
                wsHandler = new WsHandler();

            return new Agent(agentListenerImpl);
        }

        catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException | IOException ex)
        {
            logger.error("Failed to create a new agent instance", ex);
            throw new AgentInitException("Failed to initialize agent", ex.getCause());
        }
    }


    /**
     * This method is used to discover edge routers on the Local Area Network.
     * It returns a CompletableFuture that will be completed with a list of RouterInfo objects.
     *
     * @return CompletableFuture<List < RouterInfo>>
     */
    public synchronized CompletableFuture<List<RouterInfo>> discover()
    {
        return CompletableFuture.supplyAsync(mDNSDiscoveryService::listen, workerPool);
    }


    /**
     * This method is used to asynchronously discover edge routers on the Local Area Network.
     * It calls the servicesDiscovered method of the ServiceDiscoveryListener implementation.
     *
     * @param listener The ServiceDiscoveryListener implementation that will be called when the discovery is completed.
     */
    public synchronized void discover(ServiceDiscoveryListener listener)
    {
        workerPool.execute(() ->
        {
            List<RouterInfo> routers = mDNSDiscoveryService.listen();
            listener.servicesDiscovered(routers);
        });
    }


    /**
     * This method is used to connect to an edge router anonymously.
     * It returns a CompletableFuture that will be completed when the connection is established.
     * In addition, the method calls the onConnect method of the AgentListener implementation (further down the callstack).
     *
     * @param routerInfo The RouterInfo object that contains the information of the edge router to connect to.
     * @param tlsConfig  The TLSConfig object that contains the TLS configuration needed to connect to the edge router.
     * @return CompletableFuture<Void> Future that will be completed when the connection is established or failed.
     * @throws ConnectException Thrown when the agent is already connected to an edge router.
     */
    public synchronized CompletableFuture<Void> connectAnonymously(RouterInfo routerInfo, TLSConfig tlsConfig) throws ConnectException
    {
        if (this.status == AgentStatus.CONNECTED_AUTHENTICATED || this.status == AgentStatus.CONNECTED_ANONYMOUS)
            throw new ConnectException("Agent is already connected to an edge router");

        return CompletableFuture.runAsync(() -> wsHandler.connectAnonymously(routerInfo, tlsConfig, this, this), workerPool);
    }


    /**
     * This method is used to connect to an edge router authenticated.
     * It returns a CompletableFuture that will be completed when the connection is established.
     * In addition, the method calls the onConnect method of the AgentListener implementation.
     *
     * @param routerInfo The RouterInfo object that contains the information of the edge router to connect to.
     * @param tlsConfig  The mTLSConfig object that contains the TLS configuration needed to connect to the edge router.
     * @return CompletableFuture<Void> Future that will be completed when the connection is established or failed.
     * @throws ConnectException Thrown when the agent is already connected to an edge router.
     */
    public synchronized CompletableFuture<Void> connectAuthenticated(RouterInfo routerInfo, mTLSConfig tlsConfig) throws ConnectException
    {
        if (this.status == AgentStatus.CONNECTED_AUTHENTICATED || this.status == AgentStatus.CONNECTED_ANONYMOUS)
            throw new ConnectException("Agent is already connected to an edge router");

        return CompletableFuture.runAsync(() -> wsHandler.connectAuthenticated(routerInfo, tlsConfig, this, this), workerPool);
    }


    /**
     * This method is used to disconnect from the edge router.
     * It returns a CompletableFuture that will be completed when the connection is closed.
     * In addition, the method calls the onDisconnect method of the AgentListener implementation.
     *
     * @return CompletableFuture<Void> Future that will be completed when the connection is closed or failed.
     * @throws DisconnectException Thrown when the agent is not connected to any edge router.
     */
    public synchronized CompletableFuture<Void> disconnect() throws DisconnectException
    {
        if (!this.status.equals(AgentStatus.CONNECTED_AUTHENTICATED) && !this.status.equals(AgentStatus.CONNECTED_ANONYMOUS))
            throw new DisconnectException("Agent is not connected to any edge router");

        return CompletableFuture.runAsync(() -> this.connection.close(WsCodes.NORMAL_CLOSURE, "User closed connection."), workerPool);
    }


    /**
     * This method is used to subscribe to a subject.
     * The method returns a CompletableFuture that will be completed with a list of subjects that were successfully subscribed to.
     *
     * @param subject The subject to subscribe to.
     * @return CompletableFuture<List < String>> Future that will be completed with a list of subjects that were successfully subscribed to.
     * @throws NotConnectedException Thrown when the agent is not connected to any edge router.
     */
    public CompletableFuture<List<String>> subscribe(String subject) throws NotConnectedException
    {
        return subscribe(Collections.singletonList(subject));
    }


    /**
     * This method is used to subscribe to a list of subjects.
     *
     * @param subjects The list of subjects to subscribe to.
     * @return CompletableFuture<List<String>> Future that will be completed with a list of subjects that were successfully subscribed to.
     * @throws NotConnectedException Thrown when the agent is not connected to any edge router.
     */
    public CompletableFuture<List<String>> subscribe(List<String> subjects) throws NotConnectedException
    {
        if (this.status == AgentStatus.LOST_CONNECTION || this.status == AgentStatus.NOT_CONNECTED)
            throw new NotConnectedException("Agent is not connected to the router");

        return CompletableFuture.supplyAsync(() -> this.subscriptionManager.subscribe(subjects, connection), workerPool);
    }


    /**
     * This method is used to unsubscribe from a subject.
     * The method returns a CompletableFuture that will be completed with a list of subjects that were successfully unsubscribed from.
     *
     * @param subject The subject to unsubscribe from.
     * @return CompletableFuture<List < String>> Future that will be completed with a list of subjects that were successfully unsubscribed from.
     * @throws NotConnectedException Thrown when the agent is not connected to any edge router.
     */
    public CompletableFuture<List<String>> unsubscribe(String subject) throws NotConnectedException
    {
        return unsubscribe(Collections.singletonList(subject));
    }


    /**
     * This method is used to unsubscribe from a list of subjects.
     * This method returns a CompletableFuture that will be completed with a list of subjects that were successfully unsubscribed from.
     *
     * @param subjects The list of subjects to unsubscribe from.
     * @return CompletableFuture<List < String>> Future that will be completed with a list of subjects that were successfully unsubscribed from.
     * @throws NotConnectedException Thrown when the agent is not connected to any edge router.
     */
    public CompletableFuture<List<String>> unsubscribe(List<String> subjects) throws NotConnectedException
    {
        if (this.status == AgentStatus.LOST_CONNECTION || this.status == AgentStatus.NOT_CONNECTED)
            throw new NotConnectedException("Agent is not connected to the router");

        return CompletableFuture.supplyAsync(() -> this.subscriptionManager.unsubscribe(subjects, connection), workerPool);
    }


    /**
     * This method is used to subscribe to direct messages.
     * This method returns a CompletableFuture that will be completed when the subscription is successful.
     *
     * @return CompletableFuture<Void> Future that will be completed when the subscription is successful.
     * @throws NotConnectedException Thrown when the agent is not connected to any edge router.
     */
    public CompletableFuture<Void> subscribeToDM() throws NotConnectedException
    {
        if (this.status == AgentStatus.LOST_CONNECTION || this.status == AgentStatus.NOT_CONNECTED)
            throw new NotConnectedException("Agent is not connected to the router");

        return CompletableFuture.runAsync(() -> this.subscriptionManager.subscribeToDM(connection), workerPool);
    }


    /**
     * This method is used to unsubscribe from direct messages.
     * This method returns a CompletableFuture that will be completed when the unsubscription is successful.
     *
     * @return CompletableFuture<Void> Future that will be completed when the unsubscription is successful.
     * @throws NotConnectedException Thrown when the agent is not connected to any edge router.
     */
    public CompletableFuture<Void> unsubscribeFromDM() throws NotConnectedException
    {
        if (this.status == AgentStatus.LOST_CONNECTION || this.status == AgentStatus.NOT_CONNECTED)
            throw new NotConnectedException("Agent is not connected to the router");

        return CompletableFuture.runAsync(() -> this.subscriptionManager.unsubscribeFromDM(connection), workerPool);
    }


    /**
     * This method is used to send a direct message to a single destination asynchronously.
     * <p>
     * The method returns a CompletableFuture that will be completed with the message ID
     * of the sent message or an exception if the message could not be sent.
     *
     * @param destination The destination to send the message to.
     * @param payload     The payload of the message.
     * @param expires     The expiration time of the message.
     * @return CompletableFuture<String> Future that will be completed with the message ID of the sent message.
     * @throws MMSSecurityException    If the agent is not authenticated.
     * @throws NotConnectedException   If the agent is not connected to any edge router.
     * @throws MMTPValidationException If the message is not valid.
     */
    public CompletableFuture<String> sendDirectMessage(String destination, byte[] payload, Instant expires) throws MMSSecurityException, NotConnectedException, MMTPValidationException
    {
        return sendDirectMessage(List.of(destination), payload, expires);
    }


    /**
     * This method is used to send a direct message to a single destination asynchronously.
     * <p>
     * This method uses a callback to notify the caller of success or failure.
     *
     * @param destination  The destination to send the message to.
     * @param payload      The payload of the message.
     * @param expires      The expiration time of the message.
     * @param sendListener The callback to notify the caller of success or failure.
     * @throws MMSSecurityException    If the agent is not authenticated.
     * @throws NotConnectedException   If the agent is not connected to any edge router.
     * @throws MMTPValidationException If the message is not valid.
     */
    public void sendDirectMessage(String destination, byte[] payload, Instant expires, SendListener sendListener) throws MMSSecurityException, NotConnectedException, MMTPValidationException
    {
        sendDirectMessage(List.of(destination), payload, expires, sendListener);
    }


    /**
     * This method is used to send a direct message to a list of destinations asynchronously.
     * <p>
     * The method returns a CompletableFuture that will be completed with the message ID of
     * the sent message or an exception if the message could not be sent.
     *
     * @param destinations The list of destinations to send the message to.
     * @param payload      The payload of the message.
     * @param expires      The expiration time of the message.
     * @return CompletableFuture<String> Future that will be completed with the message ID of the sent message.
     * @throws MMSSecurityException    If the agent is not authenticated.
     * @throws NotConnectedException   If the agent is not connected to any edge router.
     * @throws MMTPValidationException If the message is not valid.
     */
    public CompletableFuture<String> sendDirectMessage(List<String> destinations, byte[] payload, Instant expires) throws MMSSecurityException, NotConnectedException, MMTPValidationException
    {
        if (!connection.isConnected())
            throw new NotConnectedException("Agent is not connected to the router");

        if (this.status != AgentStatus.CONNECTED_AUTHENTICATED)
            throw new MMSSecurityException("Only authenticated clients can send messages");


        String MRN = ((AuthenticatedConnection) connection).getMRN();
        DirectApplicationMessage message = MMTPUtils.createDirectApplicationMessage(destinations, MRN, payload, expires);
        MMTPValidator.validate(message);
        byte[] bytes = message.toByteArray();

        ProtocolMessage.Builder builder = ProtocolMessage.newBuilder();
        builder.setType(MessageType.DIRECT_APPLICATION_MESSAGE);
        builder.setContent(ByteString.copyFrom(bytes));
        ProtocolMessage protocolMessage = builder.build();

        CompletableFuture<String> future = new CompletableFuture<>();

        workerPool.execute(() ->
        {
            try
            {
                this.connection.send(protocolMessage);
                future.complete(message.getId());
            }

            catch (SendingException ex)
            {
                SendingException sendingException = new SendingException("Failed to send direct message", ex.getCause());
                future.completeExceptionally(sendingException);
            }
        });

        return future;
    }


    /**
     * This method is used to send a direct message to a list of destinations.
     * <p>
     * This method uses a callback to notify the caller of success or failure.
     *
     * @param destinations The list of destinations to send the message to.
     * @param payload      The payload of the message.
     * @param expires      The expiration time of the message.
     * @throws MMSSecurityException    If the agent is not authenticated.
     * @throws NotConnectedException   If the agent is not connected to any edge router.
     * @throws MMTPValidationException If the message is not valid.
     */
    public void sendDirectMessage(List<String> destinations, byte[] payload, Instant expires, SendListener sendListener) throws MMSSecurityException, NotConnectedException, MMTPValidationException
    {
        if (!connection.isConnected())
        {
            throw new NotConnectedException("Agent is not connected to the router");
        }

        if (this.status != AgentStatus.CONNECTED_AUTHENTICATED)
        {
            throw new MMSSecurityException("Only authenticated clients can send messages");
        }

        String MRN = ((AuthenticatedConnection) connection).getMRN();

        DirectApplicationMessage message = MMTPUtils.createDirectApplicationMessage(destinations, MRN, payload, expires);
        MMTPValidator.validate(message);
        byte[] bytes = message.toByteArray();

        ProtocolMessage.Builder builder = ProtocolMessage.newBuilder();
        builder.setType(MessageType.DIRECT_APPLICATION_MESSAGE);
        builder.setContent(ByteString.copyFrom(bytes));
        ProtocolMessage protocolMessage = builder.build();


        workerPool.execute(() ->
        {
            try
            {
                this.connection.send(protocolMessage);
                sendListener.onSuccess(message.getId());
            }

            catch (SendingException ex)
            {
                SendingException sendingException = new SendingException("Failed to send direct message", ex.getCause());
                sendListener.onFailure(sendingException);
            }
        });
    }


    /**
     * This method is used to send subject cast messages.
     *
     * @param subject The subject to send the message to.
     * @param payload The payload of the message.
     * @param expires The expiration time of the message.
     * @return CompletableFuture<String> Future that will be completed with the message ID of the sent message.
     * @throws NotConnectedException   If the agent is not connected to any edge router.
     * @throws MMSSecurityException    If the agent is not authenticated.
     * @throws MMTPValidationException If the message is not valid.
     */
    public CompletableFuture<String> publish(String subject, byte[] payload, Instant expires) throws NotConnectedException, MMSSecurityException, MMTPValidationException
    {
        if (!connection.isConnected())
        {
            throw new NotConnectedException("Agent is not connected to the router");
        }

        if (this.status != AgentStatus.CONNECTED_AUTHENTICATED)
        {
            throw new MMSSecurityException("Only authenticated clients can send messages");
        }

        String MRN = ((AuthenticatedConnection) connection).getMRN();
        SubjectCastApplicationMessage message = MMTPUtils.createSubjectCastApplicationMessage(subject, MRN, payload, expires);
        MMTPValidator.validate(message);
        byte[] bytes = message.toByteArray();

        ProtocolMessage.Builder builder = ProtocolMessage.newBuilder();
        builder.setType(MessageType.SUBJECT_CAST_APPLICATION_MESSAGE);
        builder.setContent(ByteString.copyFrom(bytes));
        ProtocolMessage protocolMessage = builder.build();

        CompletableFuture<String> future = new CompletableFuture<>();
        workerPool.execute(() ->
        {
            try
            {
                connection.send(protocolMessage);
                future.complete(message.getId());
            }

            catch (SendingException e)
            {
                SendingException sendingException = new SendingException("Failed to send subject cast message", e.getCause());
                future.completeExceptionally(sendingException);
            }
        });

        return future;
    }


    /**
     * This method is used to send subject cast messages.
     *
     * @param subject      The subject to send the message to.
     * @param payload      The payload of the message.
     * @param expires      The expiration time of the message.
     * @param sendListener The listener to notify of success or failure.
     * @throws NotConnectedException   If the agent is not connected to any edge router.
     * @throws MMSSecurityException    If the agent is not authenticated.
     * @throws MMTPValidationException If the message is not valid.
     */
    public void publish(String subject, byte[] payload, Instant expires, SendListener sendListener) throws NotConnectedException, MMSSecurityException, MMTPValidationException
    {
        if (!connection.isConnected())
            throw new NotConnectedException("Agent is not connected to the router");

        if (this.status != AgentStatus.CONNECTED_AUTHENTICATED)
            throw new MMSSecurityException("Only authenticated clients can send messages");

        String MRN = ((AuthenticatedConnection) connection).getMRN();
        SubjectCastApplicationMessage message = MMTPUtils.createSubjectCastApplicationMessage(subject, MRN, payload, expires);
        MMTPValidator.validate(message);
        byte[] bytes = message.toByteArray();

        ProtocolMessage.Builder builder = ProtocolMessage.newBuilder();
        builder.setType(MessageType.SUBJECT_CAST_APPLICATION_MESSAGE);
        builder.setContent(ByteString.copyFrom(bytes));
        ProtocolMessage protocolMessage = builder.build();

        workerPool.execute(() ->
        {
            try
            {
                connection.send(protocolMessage);
                sendListener.onSuccess(message.getId());
            }

            catch (SendingException e)
            {
                SendingException sendingException = new SendingException("Failed to send subject cast message", e.getCause());
                sendListener.onFailure(sendingException);
            }
        });
    }


    /**
     * This method is used to acquire a list of all the subscriptions of the agent.
     *
     * @return List<String> The list of all the subscriptions of the agent.
     */
    public List<String> getSubscriptions()
    {
        return subscriptionManager.getSubscriptions();
    }


    /**
     * This method is used to acquire the status of the agent.
     *
     * @return AgentStatus The status of the agent.
     */
    public AgentStatus getStatus()
    {
        return status;
    }


    /* Implementation of connection and message listener interfaces, handles lifecycle events. */


    /**
     * Implementation of the ConnectionListener interface.
     * <p>
     * This method is called when the connection to the router is established.
     * It sets the status of the agent according to the type of connection (authenticated or anonymous).
     * It then calls the onConnect method of the adapter supplied by the user.
     *
     * @param connection The connection to the router.
     */
    @Override
    public void onConnectionEstablished(Connection connection)
    {
        if (connection instanceof AuthenticatedConnection)
        {
            this.connection = connection;
            status = AgentStatus.CONNECTED_AUTHENTICATED;
            logger.info("Agent successfully connected to router in authenticated mode.");
        }

        else if (connection instanceof AnonymousConnection)
        {
            this.connection = connection;
            status = AgentStatus.CONNECTED_ANONYMOUS;
            logger.info("Agent successfully connected to router in anonymous mode.");
        }

        adapter.onConnect(status);
    }


    /**
     * Implementation of the ConnectionListener interface.
     * <p>
     * This method is called if the initial handshake with the router fails / no connection could be established.
     * It sets the status of the agent to NOT_CONNECTED.
     * It then calls the onHandshakeError method of the adapter supplied by the user.
     *
     * @param reason The reason for the failure.
     */
    @Override
    public void onConnectError(DisconnectionReason reason)
    {
        connection = null;
        status = AgentStatus.NOT_CONNECTED;
        logger.error("The connection to the router failed. Reason: " + reason.getReason());
        adapter.onHandshakeError(reason);
    }


    /**
     * Implementation of the ConnectionListener interface.
     * <p>
     * This method is called if the connection to the router is lost after it was established.
     * It sets the status of the agent to LOST_CONNECTION.
     * It then calls the onDisconnect method of the adapter supplied by the user.
     *
     * @param reason The reason for the disconnection.
     */
    @Override
    public void onConnectionLost(DisconnectionReason reason)
    {
        connection = null;
        status = AgentStatus.LOST_CONNECTION;
        logger.error("The connection to the router was lost. Reason: " + reason.getReason());
        adapter.onDisconnect(reason);
    }


    /**
     * Implementation of the MessageListener interface.
     * <p>
     * This method is called when a direct message is received (after processing the message).
     * It checks if the agent is connected in authenticated mode and if the message is intended for it.
     * If so, it calls the onDirectMessage method of the adapter supplied by the user.
     *
     * @param messageId    The id of the message.
     * @param destinations The list of destinations of the message.
     * @param sender       The sender of the message.
     * @param expires      The expiration time of the message.
     * @param message      The payload of the message.
     */
    @Override
    public void onDirectMessage(String messageId, List<String> destinations, String sender, Instant expires, byte[] message)
    {
        if (status == AgentStatus.CONNECTED_AUTHENTICATED)
        {
            String MRN = ((AuthenticatedConnection) connection).getMRN();

            if (destinations.contains(MRN))
            {
                if (subscriptionManager.wantsDirectMessages())
                    adapter.onDirectMessage(messageId, sender, expires, message);
                else
                    logger.error("The agent received a direct message while not subscribed to direct messages, ignoring message.");
            }

            else
                logger.error("The agent received a direct message that was not intended for it, ignoring message.");
        }

        else
            logger.error("The agent received a direct message while not connected in authenticated mode, ignoring message.");
    }


    /**
     * Implementation of the MessageListener interface.
     * <p>
     * This method is called when a subject cast message is received (after processing the message).
     * It checks if the agent is subscribed to the subject of the message.
     * If so, it calls the onSubjectCastMessage method of the adapter supplied by the user.
     *
     * @param messageId The id of the message.
     * @param sender    The sender of the message.
     * @param subject   The subject of the message.
     * @param expires   The expiration time of the message.
     * @param message   The payload of the message.
     */
    @Override
    public void onSubjectCastMessage(String messageId, String sender, String subject, Instant expires, byte[] message)
    {
        boolean isSubscribed = subscriptionManager.isSubscribed(subject);

        if (isSubscribed)
            adapter.onSubjectCastMessage(messageId, sender, subject, expires, message);

        else
            logger.error("The agent received a subject cast message that it was not subscribed to, ignoring message.");
    }


    /* Implementation of the subscription listener interface */


    /**
     * Implementation of the SubscriptionListener interface.
     * <p>
     * This method is called when the agent successfully subscribes to a subject.
     * It calls the onSubscriptionSuccess method of the adapter supplied by the user.
     *
     * @param subjects The list of subjects that the agent subscribed to.
     */
    @Override
    public void onSubscriptionSuccess(List<String> subjects)
    {
        logger.info("Agent subscribed to subjects: " + subjects);
        adapter.onSubscriptionSuccess(subjects);
    }


    /**
     * Implementation of the SubscriptionListener interface.
     * <p>
     * This method is called when the agent fails to subscribe to a subject.
     * It calls the onSubscriptionFailure method of the adapter supplied by the user.
     *
     * @param subjects The list of subjects that the agent failed to subscribe to.
     * @param reason   The reason for the failure.
     * @param cause    The cause of the failure.
     */
    @Override
    public void onSubscriptionFailure(List<String> subjects, String reason, Throwable cause)
    {
        logger.info("Agent subscription failed for subjects: " + subjects);
        adapter.onSubscriptionFailure(subjects, reason, cause);
    }


    /**
     * Implementation of the SubscriptionListener interface.
     * <p>
     * This method is called when the agent is unsubscribed from a subject.
     * It calls the onSubscriptionRemoved method of the adapter supplied by the user.
     *
     * @param subjects The list of subjects that the agent was unsubscribed from.
     */
    @Override
    public void onSubscriptionRemoved(List<String> subjects)
    {
        logger.info("Agent unsubscribed from subjects: " + subjects);
        adapter.onSubscriptionRemoved(subjects);
    }


    /**
     * Implementation of the SubscriptionListener interface.
     * <p>
     * This method is called when the agent successfully subscribes to direct messages.
     * It calls the onDirectMessageSubscriptionChanged method of the adapter supplied by the user.
     *
     * @param wantsDirectMessages True if the agent wants to receive direct messages, false otherwise.
     */
    @Override
    public void onDirectMessageSubscriptionChanged(boolean wantsDirectMessages)
    {
        logger.info("Agent direct message subscription changed to: " + wantsDirectMessages);
        adapter.onDirectMessageSubscriptionChanged(wantsDirectMessages);
    }


    /**
     * Implementation of the SubscriptionListener interface.
     * <p>
     * This method is called when the agent fails to subscribe to direct messages.
     * It calls the onDirectMessageSubscriptionFailure method of the adapter supplied by the user.
     *
     * @param reason The reason for the failure.
     * @param cause  The cause of the failure.
     */
    @Override
    public void onDirectMessageSubscriptionFailure(String reason, Throwable cause)
    {
        logger.error("Agent direct message subscription failed: " + reason);
        adapter.onDirectMessageSubscriptionFailure(reason, cause);
    }
}


