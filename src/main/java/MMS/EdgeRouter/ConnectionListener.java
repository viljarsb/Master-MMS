package MMS.EdgeRouter;

import java.nio.ByteBuffer;

/**
 * The Edge Router forward engine implements this interface,
 * it allows the Edge Router to be notified when a new connection is established
 * or when a connection is terminated. In addition, the implementation can notify the
 * forwarding engine once it receives a message. It can be used with any type of connection.
 */
public interface ConnectionListener
{
    void onConnection(RemoteConnection connection, ConnectionStatus status);
    void onDisconnection(RemoteConnection connection);
    void onConditionChange(RemoteConnection connection, ConnectionStatus status);
    void onMessage(RemoteConnection connection, ByteBuffer message);
}
