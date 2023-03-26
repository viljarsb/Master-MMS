package MMS.Client.Interfaces;

import MMS.Client.Connections.Connection;
import MMS.Client.Connections.DisconnectionReason;

/**
 * A listener for connection events.
 */
public interface ConnectionListener
{
    void onConnectionEstablished(Connection connection);
    void onConnectionLost(DisconnectionReason reason);
    void onConnectError(DisconnectionReason reason);
}
