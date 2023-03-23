package MMS.Client;


import MMS.Client.Connections.AnonymousConnection;
import MMS.Client.Connections.Connection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.time.Instant;


public abstract class AgentAdapter
{
    private static final Logger logger = LogManager.getLogger(AgentAdapter.class);
    private final ConnectionListener eventListener;


    protected AgentAdapter(ConnectionListener connectionListenerCallback)
    {
        this.eventListener = connectionListenerCallback;
    }


    void onAuthenticatedConnectionDefault(Connection connection)
    {
        InetSocketAddress remoteAddress = connection.getRemoteAddress();
        logger.info("Connection established with remote host: " + remoteAddress.getAddress().getHostAddress());
        eventListener.onConnectionEstablished(connection);
        onAuthenticatedConnection();
    }


    void onAnonymousConnectionDefault(AnonymousConnection connection)
    {
        InetSocketAddress remoteAddress = connection.getRemoteAddress();
        logger.info("Anonymous connection established with remote host: " + remoteAddress.getAddress().getHostAddress());
        eventListener.onConnectionEstablished(connection);
        onAnonymousConnection();
    }


    void onDisconnectDefault(String reason, int statusCode)
    {
        DisconnectionReason disconnectionReason = new DisconnectionReason(reason, statusCode);
        eventListener.onDisconnect();
        onDisconnect(disconnectionReason);
    }


    void onConnectionErrorDefault(String reason, Throwable cause)
    {
        eventListener.onDisconnect();
        onConnectionError(reason, cause);
    }


    // Abstract methods that the users of the library have to implement
    protected abstract void onAnonymousConnection();
    protected abstract void onAuthenticatedConnection();
    protected abstract void onDisconnect(DisconnectionReason reason);
    protected abstract void onDirectMessage(String messageId, String sender, Instant expires, byte[] message);
    protected abstract void onSubjectCastMessage(String messageId, String sender, String subject, Instant expires, byte[] message);
    protected abstract void onConnectionError(String reason, Throwable cause);
}
