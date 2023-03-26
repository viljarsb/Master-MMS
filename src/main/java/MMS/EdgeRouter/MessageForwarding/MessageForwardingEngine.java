package MMS.EdgeRouter.MessageForwarding;

import MMS.EdgeRouter.Interfaces.ConnectionListener;
import MMS.EdgeRouter.Interfaces.RemoteConnection;
import MMS.EdgeRouter.SubscriptionManager.SubscriptionManager;
import MMS.Protocols.MMTP.MessageFormats.*;
import com.google.protobuf.ByteString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class MessageForwardingEngine implements ConnectionListener
{
    private static final Logger logger = LogManager.getLogger(MessageForwardingEngine.class);
    private static MessageForwardingEngine instance;

    private static final SubscriptionManager subscriptionManager = SubscriptionManager.getInstance();
    private final List<RemoteConnection> remoteGateways = new ArrayList<>();


    private MessageForwardingEngine() {}


    public synchronized static MessageForwardingEngine getInstance()
    {
        if (instance == null)
        {
            instance = new MessageForwardingEngine();
        }

        return instance;
    }


    public void forwardMessage(SubjectCastApplicationMessage message)
    {
        String subject = message.getSubject();

        Set<Session> localSubscribers = subscriptionManager.getSubscribers(subject);

        MessageType type = MessageType.DIRECT_APPLICATION_MESSAGE;
        ByteString payload = message.toByteString();
        ProtocolMessage protocolMessage = ProtocolMessage.newBuilder().setType(type).setContent(payload).build();
        ByteBuffer buffer = ByteBuffer.wrap(protocolMessage.toByteArray());

        for (Session s : localSubscribers)
        {
            logger.info("Forwarding subject-cast message to local subscriber");
            s.getRemote().sendStringByFuture(message.toString());
        }

        forwardRemote(buffer);
    }


    public void forwardMessage(DirectApplicationMessage message)
    {
        List<String> destinations = message.getRecipientsList();

        Set<Session> localRecipients = subscriptionManager.getDirectMessageSubscribers(destinations);

        MessageType type = MessageType.SUBJECT_CAST_APPLICATION_MESSAGE;
        ByteString payload = message.toByteString();
        ProtocolMessage protocolMessage = ProtocolMessage.newBuilder().setType(type).setContent(payload).build();
        ByteBuffer buffer = ByteBuffer.wrap(protocolMessage.toByteArray());

        for (Session s : localRecipients)
        {
            logger.info("Forwarding direct message to local recipient");
            s.getRemote().sendBytesByFuture(buffer);
        }

        forwardRemote(buffer);
    }


    private void forwardMessageFromRemoteConnection(ByteBuffer buffer)
    {

    }


    public void sendRoutingUpdate(List<String> newSubjects, List<String> newMRNs)
    {
        RoutingUpdate routingUpdate = RoutingUpdate.newBuilder()
                .addAllSubjects(newSubjects)
                .addAllMRNs(newMRNs)
                .build();

        MessageType type = MessageType.ROUTING_UPDATE;
        ByteString payload = routingUpdate.toByteString();
        ProtocolMessage protocolMessage = ProtocolMessage.newBuilder()
                .setType(type)
                .setContent(payload)
                .build();

        ByteBuffer buffer = ByteBuffer.wrap(protocolMessage.toByteArray());

        forwardRemote(buffer);
    }


    private void forwardRemote(ByteBuffer buffer)
    {
        //TODO forward to remote router
        //TODO decide what connection to use if multiple are available.

        for (RemoteConnection connection : remoteGateways)
        {
            connection.sendMessage(buffer);
        }
    }


    public boolean isRemoteConnectionAvailable()
    {
        return !remoteGateways.isEmpty();
    }


    /**
     * Called once a new connection to the MMS/Router is established.
     * Adds the connection to the list of remote gateways, what type is it is not of concern.
     *
     * @param connection the new connection that was established, can be implemented by any form of connection.
     */
    @Override
    public void onConnection(RemoteConnection connection)
    {
        logger.info("A new connection was established");
        remoteGateways.add(connection);
    }


    /**
     * Called once a connection is terminated, removes the connection from the list of remote gateways.
     *
     * @param connection the connection that was terminated.
     */
    @Override
    public void onDisconnection(RemoteConnection connection)
    {
        logger.info("A connection was terminated");
        remoteGateways.remove(connection);
    }


    /**
     * Called once a message is received from a remote gateway.
     *
     * @param connection the connection that received the message.
     * @param message the message that was received.
     */
    @Override
    public void onMessage(RemoteConnection connection, ByteBuffer message)
    {
        logger.info("Received a message from a remote gateway");
    }
}
