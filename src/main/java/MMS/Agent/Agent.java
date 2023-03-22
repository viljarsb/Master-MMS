package MMS.Agent;

import MMS.Agent.Exceptions.*;
import MMS.Agent.ServiceDiscoveryListner.RouterInfo;
import MMS.Agent.ServiceDiscoveryListner.ServiceDiscoveryListener;
import MMS.Agent.SubscriptionManager.SubscriptionManager;
import MMS.Agent.WebSocketEndpointManager.TLSConfigurator;
import MMS.Agent.WebSocketEndpointManager.WebSocketEndpointManager;

import MMS.MMTP.*;
import MMS.Misc.MrnValidator;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * The Agent class represents a client in the MMS.
 * It is responsible for managing connections, subscriptions, and message sending.
 * An Agent can connect to a router either anonymously or in an authenticated manner,
 * subscribe to subjects, and send directed or subject cast messages.
 * <p>
 * To handle incoming messages, provide a {@link AgentCallback} implementation
 * to the Agent during instantiation.
 */
public class Agent
{
    private final WebSocketEndpointManager webSocketEndpointManager;
    private final ServiceDiscoveryListener serviceDiscoveryListener;
    private final SubscriptionManager subscriptionManager;

    private final TLSConfigurator tlsConfigurator;
    private AgentStatus status;
    private String clientMRN;


    static
    {
        Security.addProvider(new BouncyCastleProvider());
    }


    /**
     * Constructs a new Agent with the specified message callback.
     *
     * @param callback The {@link AgentCallback} implementation that will handle
     *                 incoming directed and subject cast messages.
     * @throws AgentInitException If an error occurs during initialization.
     */
    public Agent(AgentCallback callback, TLSConfig config) throws AgentInitException
    {
        try
        {
            this.tlsConfigurator = new TLSConfigurator(config);
            this.webSocketEndpointManager = new WebSocketEndpointManager(callback, tlsConfigurator);
            this.serviceDiscoveryListener = new ServiceDiscoveryListener();
            this.subscriptionManager = new SubscriptionManager();

            this.status = AgentStatus.DISCONNECTED;
            this.clientMRN = null;
        }

        catch (Exception ex)
        {
            throw new AgentInitException(ex.getMessage(), ex.getCause());
        }
    }



    /**
     * Discovers available routers in the network.
     *
     * @return A list of {@link RouterInfo} objects representing discovered routers.
     */
    public List<RouterInfo> discover()
    {
        return serviceDiscoveryListener.listen();
    }


    /**
     * Connects to the specified router anonymously.
     *
     * @param routerInfo The {@link RouterInfo} object representing the router to connect to.
     * @throws ConnectException If an error occurs while establishing the connection.
     */
    public void connectAnonymously(RouterInfo routerInfo) throws ConnectException
    {
        webSocketEndpointManager.connectAnonymously(routerInfo, tlsConfigurator);
        this.status = AgentStatus.CONNECTED_ANONYMOUSLY;
    }


    /**
     * Connects to the specified router using authentication.
     *
     * @param routerInfo The {@link RouterInfo} object representing the router to connect to.
     * @throws ConnectException If an error occurs while establishing the connection.
     */
    public void connectAuthenticated(RouterInfo routerInfo) throws ConnectException
    {
        String clientMRN = webSocketEndpointManager.connectAuthenticated(routerInfo);
        this.status = AgentStatus.CONNECTED_AUTHENTICATED;
        this.clientMRN = clientMRN;
    }


    /**
     * Disconnects the Agent from the currently connected router.
     *
     * @throws DisconnectException If an error occurs while disconnecting.
     */
    public void disconnect() throws DisconnectException
    {
        webSocketEndpointManager.disconnect();

        if (status == AgentStatus.CONNECTED_AUTHENTICATED) this.clientMRN = null;

        this.status = AgentStatus.DISCONNECTED;
        this.clientMRN = null;

        subscriptionManager.clearSubscriptions();
    }


    /**
     * Subscribes the Agent to a single subject.
     *
     * @param subject The subject to subscribe to.
     * @throws NotConnectedException   If the Agent is not connected to a router.
     * @throws InvalidSubjectException If the provided subject is invalid.
     * @throws MessageSendingException If there's an error sending the subscription message.
     */
    public void subscribe(String subject) throws NotConnectedException, InvalidSubjectException, MessageSendingException
    {
        if (status == AgentStatus.DISCONNECTED)
        {
            throw new NotConnectedException("Cannot subscribe while disconnected");
        }

        if (webSocketEndpointManager.isConnected())
        {
            this.status = AgentStatus.DISCONNECTED;
            throw new NotConnectedException("Connection to router lost");
        }

        try
        {
            subscriptionManager.addSubscription(subject);
        }

        catch (IllegalArgumentException ex)
        {
            throw new InvalidSubjectException("Can not subscribe to subject: " + subject, ex);
        }

        Subscribe subscribe = Subscribe.newBuilder().addInterests(subject).build();

        sendBinary(subscribe);
    }


    /**
     * Subscribes the Agent to a list of subjects.
     *
     * @param subjects The list of subjects to subscribe to.
     * @throws InvalidSubjectException If any of the provided subjects are invalid.
     * @throws NotConnectedException   If the Agent is not connected to a router.
     * @throws MessageSendingException If there's an error sending the subscription message.
     */
    public void subscribe(List<String> subjects) throws InvalidSubjectException, NotConnectedException, MessageSendingException
    {
        if (status == AgentStatus.DISCONNECTED)
        {
            throw new NotConnectedException("Cannot subscribe while disconnected");
        }

        if (webSocketEndpointManager.isConnected())
        {
            this.status = AgentStatus.DISCONNECTED;
            throw new NotConnectedException("Connection to router lost");
        }

        for (String subject : subjects)
        {
            if (subject.length() > 100 || subject.length() < 1)
            {
                throw new InvalidSubjectException("Subject must be between 1 and 100 characters");
            }
        }

        subscriptionManager.addSubscriptions(subjects);

        Subscribe subscribe = Subscribe.newBuilder().addAllInterests(subjects).build();

        sendBinary(subscribe);
    }


    /**
     * Unsubscribes the Agent from a single subject.
     *
     * @param subject The subject to unsubscribe from.
     * @throws NotConnectedException   If the Agent is not connected to a router.
     * @throws MessageSendingException If there's an error sending the unsubscription message.
     */
    public void unsubscribe(String subject) throws NotConnectedException, MessageSendingException
    {
        try
        {
            subscriptionManager.removeSubscription(subject);
        }

        catch (IllegalArgumentException ex)
        {
            throw new IllegalArgumentException("Invalid Subject: " + subject);
        }

        Unsubscribe unsubscribe = Unsubscribe.newBuilder().addInterests(subject).build();

        sendBinary(unsubscribe);
    }


    /**
     * Unsubscribes the Agent from a list of subjects.
     *
     * @param subjects The list of subjects to unsubscribe from.
     * @throws NotConnectedException   If the Agent is not connected to a router.
     * @throws MessageSendingException If there's an error sending the unsubscription message.
     */
    public void unsubscribe(List<String> subjects) throws NotConnectedException, MessageSendingException
    {
        try
        {
            subscriptionManager.removeSubscriptions(subjects);
        }

        catch (IllegalArgumentException ex)
        {
            throw new IllegalArgumentException("Invalid Subject: " + subjects);
        }

        Unsubscribe unsubscribe = Unsubscribe.newBuilder().addAllInterests(subjects).build();

        sendBinary(unsubscribe);
    }



    public void subscribeDirectMessages() throws AnonymousConnectionException, NotConnectedException
    {
        if(this.status == AgentStatus.DISCONNECTED)
        {
            throw new NotConnectedException("Cannot subscribe while disconnected");
        }

        else if(this.status == AgentStatus.CONNECTED_ANONYMOUSLY)
        {
            throw new AnonymousConnectionException("Cannot subscribe to direct messages while connected anonymously");
        }
    }

    public void unsubscribeDirectMessages() throws AnonymousConnectionException, NotConnectedException
    {
        if(this.status == AgentStatus.DISCONNECTED)
        {
            throw new NotConnectedException("Cannot unsubscribe while disconnected");
        }

        else if(this.status == AgentStatus.CONNECTED_ANONYMOUSLY)
        {
            throw new AnonymousConnectionException("Cannot unsubscribe from direct messages while connected anonymously");
        }
    }


    public String sendDirectMessage(String destination, byte[] message, Instant expires) throws AnonymousConnectionException, NotConnectedException, MessageSendingException
    {
        if (this.status == AgentStatus.CONNECTED_ANONYMOUSLY)
        {
            throw new AnonymousConnectionException("Cannot send messages while connected anonymously");
        }

        if (!MrnValidator.validate(destination))
        {
            throw new MessageSendingException("Invalid destination MRN");
        }

        ApplicationMessage.Builder builder = ApplicationMessage.newBuilder().setRecipient(destination);

        return sendApplicationMessage(builder, message, expires);
    }


    public String sendSubjectCastMessage(String subject, byte[] message, Instant expires) throws AnonymousConnectionException, NotConnectedException, MessageSendingException
    {
        if (this.status == AgentStatus.CONNECTED_ANONYMOUSLY)
        {
            throw new AnonymousConnectionException("Cannot send messages while connected anonymously");
        }

        if (subject == null || subject.length() > 100 || subject.length() < 1)
        {
            throw new MessageSendingException("Subject must be between 1 and 100 characters");
        }

        ApplicationMessage.Builder builder = ApplicationMessage.newBuilder().setSubject(subject);

        return sendApplicationMessage(builder, message, expires);
    }


    private String sendApplicationMessage(ApplicationMessage.Builder builder, byte[] message, Instant expires) throws NotConnectedException, MessageSendingException
    {
        String messageId = UUID.randomUUID().toString();

        builder.setSender(this.clientMRN).setId(messageId).setPayload(ByteString.copyFrom(message));

        if (expires != null && (expires.isBefore(Instant.now()) || expires.isAfter(Instant.now().plus(30, ChronoUnit.DAYS))))
            throw new MessageSendingException("Expires must be in the future and not more than 30 days from now");

        if (expires != null) builder.setExpires(expires.getEpochSecond());

        ApplicationMessage applicationMessage = builder.build();
        sendBinary(applicationMessage);
        return messageId;
    }


    /**
     * Sends a binary message to the router.
     *
     * @param message The message to be sent, as an instance of a protobuf-generated Message class.
     *                The message should be a MMTP protocol message.
     * @throws NotConnectedException   If the Agent is not connected to a router.
     * @throws MessageSendingException If there's an error sending the binary message.
     */
    private void sendBinary(Message message) throws NotConnectedException, MessageSendingException
    {
        if (status == AgentStatus.DISCONNECTED) throw new NotConnectedException("Agent is not connected to a router.");


        try
        {
            ProtocolMessageEnvelope.Builder builder = ProtocolMessageEnvelope.newBuilder();
            ByteString binaryMessage = ByteString.copyFrom(message.toByteArray());

            switch (message.getDescriptorForType().getName())
            {
                case "ApplicationMessage" ->
                {
                    builder.setContent(ByteString.copyFrom(binaryMessage.toByteArray()));
                    builder.setType(MessageType.APPLICATION_MESSAGE);
                }

                case "DirectMessagePreference" ->
                {
                    builder.setContent(ByteString.copyFrom(binaryMessage.toByteArray()));
                    builder.setType(MessageType.DIRECT_MESSAGE_PREFERENCE);
                }

                case "Subscribe" ->
                {
                    builder.setContent(ByteString.copyFrom(binaryMessage.toByteArray()));
                    builder.setType(MessageType.SUBSCRIBE);
                }

                case "Unsubscribe" ->
                {
                    builder.setContent(ByteString.copyFrom(binaryMessage.toByteArray()));
                    builder.setType(MessageType.UNSUBSCRIBE);
                }
                default -> throw new MessageSendingException("Invalid message type: " + message.getDescriptorForType().getName());
            }

            ProtocolMessageEnvelope protocolMessage = builder.build();
            byte[] protocolMessageBinary = protocolMessage.toByteArray();
            webSocketEndpointManager.send(protocolMessageBinary);
        }

        catch (NotConnectedException ex)
        {
            this.status = AgentStatus.DISCONNECTED;
            throw new NotConnectedException("Connection to router lost");
        }

        catch (MessageSendingException ex)
        {
            throw new MessageSendingException("Failed to send message: " + message.getDescriptorForType().getName());
        }
    }


    public AgentStatus getStatus()
    {
        return status;
    }


    /**
     * Enum representing the connection status of the Agent.
     */
    public enum AgentStatus
    {
        DISCONNECTED, CONNECTED_ANONYMOUSLY, CONNECTED_AUTHENTICATED
    }
}
