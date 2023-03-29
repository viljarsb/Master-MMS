package MMS.EdgeRouter;


import com.google.protobuf.Message;

import java.util.Set;

/**
 * This interface should be implemented by MMS gateway connections.
 * It enables the Edge Router to send messages via the connection, without requiring knowledge of the implementation details.
 * Additionally, it provides the ability for the Edge Router to close the connection and check the status of the connection.
 */
public interface RemoteConnection
{
    void send(Message message);
    void addSubscriptions(Set<String> subjects, Set<String> MRNs);
    void removeSubscriptions(Set<String> subjects, Set<String> MRNs);
    void shutdown();
}