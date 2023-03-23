package MMS.Client;

import MMS.Client.Connections.Connection;

public interface ConnectionListener
{
    void onConnectionEstablished(Connection connection);

    void onConnectionLost();

    void onDisconnect(DisconnectionReason reason);
}
