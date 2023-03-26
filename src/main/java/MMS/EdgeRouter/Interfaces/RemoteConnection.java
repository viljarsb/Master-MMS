package MMS.EdgeRouter.Interfaces;

import java.nio.ByteBuffer;


/**
 * This interface should be implemented by MMS gateway connections.
 * It enables the Edge Router to send messages via the connection, without requiring knowledge of the implementation details.
 * Additionally, it provides the ability for the Edge Router to close the connection and check the status of the connection.
 */
public interface RemoteConnection
{
    void sendMessage(ByteBuffer message);
    void getStatus(Boolean status);
    void close();
}
