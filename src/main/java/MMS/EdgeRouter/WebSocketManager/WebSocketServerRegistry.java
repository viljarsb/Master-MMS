package MMS.EdgeRouter.WebSocketManager;

import org.eclipse.jetty.server.Server;

import java.net.URI;
import java.util.HashMap;

public class WebSocketServerRegistry
{
    private final HashMap<String, Server> deployedServers;

    private WebSocketServerRegistry()
    {
        deployedServers = new HashMap<>();
    }

    public static WebSocketServerRegistry getRegistry()
    {
        return new WebSocketServerRegistry();
    }

    public void deployServer(String serverName, Server server)
    {
        deployedServers.put(serverName, server);
    }

    public Server getServer(String serverName)
    {
        return deployedServers.get(serverName);
    }

    public void removeServer(String serverName)
    {
        deployedServers.remove(serverName);
    }
}
