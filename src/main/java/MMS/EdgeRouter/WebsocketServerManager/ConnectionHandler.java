package MMS.EdgeRouter.WebsocketServerManager;

import MMS.EdgeRouter.SubscriptionManager.SubscriptionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;


import java.util.concurrent.ConcurrentHashMap;

public class ConnectionHandler
{
    private static final Logger logger = LogManager.getLogger(ConnectionHandler.class);

    private static ConnectionHandler instance;

    private final ConcurrentHashMap<String, ConnectionState> connections;

    private final SubscriptionManager subscriptionManager;


    private ConnectionHandler()
    {
        connections = new ConcurrentHashMap<>();
        subscriptionManager = SubscriptionManager.getManager();
    }


    public static ConnectionHandler getHandler()
    {
        if (instance == null)
        {
            instance = new ConnectionHandler();
            logger.info("Connection Handler Created");
        }
        return instance;
    }


    public void addConnection(String connectionID, Session session)
    {
        ConnectionState connectionState = new ConnectionState(session);
        connections.put(connectionID, connectionState);
        //subscriptionManager.addClient(connectionState);
        logger.debug("Connection Added: " + session.getRemoteAddress().getAddress().getHostAddress());
    }


    public void closeConnection(String connectionID, int statusCode, String reason)
    {
        ConnectionState connectionState = connections.remove(connectionID);

        if (connectionState != null)
        {
            Session session = connectionState.getSession();
            session.close(statusCode, reason);
            //subscriptionManager.removeClient(connectionState);
            logger.info("Connection Closed: " + session.getRemoteAddress().getAddress().getHostAddress());
        }
    }


    public void removeConnection(String connectionID)
    {
        connections.remove(connectionID);
    }


    public ConnectionState getConnectionState(String connectionID)
    {
        return connections.get(connectionID);
    }


    public void closeAllConnections()
    {
        logger.info("Closing all connections");
        for (String connectionID : connections.keySet())
        {
            closeConnection(connectionID, StatusCode.NORMAL, "Edge Router is shutting down");
        }
    }


    public int numberOfConnections()
    {
        int count = 0;

        for(ConnectionState state : connections.values())
        {
            if(state.isConnected())
            {
                count++;
            }
        }

        return count;
    }


}


