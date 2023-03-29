package MMS.EdgeRouter;

import net.maritimeconnectivity.pki.PKIIdentity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@code ConnectionHandler} class is a singleton that manages WebSocket connections
 * and their associated {@code AgentConnection} objects. It provides methods for adding,
 * removing, and retrieving WebSocket sessions and their states.
 */
public class ConnectionHandler
{
    private static final Logger logger = LogManager.getLogger(ConnectionHandler.class);
    private static ConnectionHandler instance;

    private final ConcurrentHashMap<Session, AgentConnection> connections = new ConcurrentHashMap<>();
    private final UsageMonitor usageMonitor = UsageMonitor.getMonitor();
    private final SubscriptionManager subscriptionManager = SubscriptionManager.getInstance();


    private ConnectionHandler()
    {
        logger.info("Connection Handler Initialized");
    }


    public synchronized static ConnectionHandler getHandler()
    {
        if (instance == null)
            instance = new ConnectionHandler();

        return instance;
    }


    public void addConnection(Session session)
    {
        AgentConnection connection = new AgentConnection(session);
        connections.put(session, connection);

        PKIIdentity identity = connection.getIdentity();

        if (identity != null)
        {
            logger.debug("Agent added: Agent ID = {} - connected in authenticated mode.\nCN: {}\n{}", connection.getAgentId(), identity.getCn(), identity.getMrn());
            subscriptionManager.addDirectMessageSubscription(session);
        }

        else
        {
            logger.info("Agent added: Agent ID = {} - connected in unauthenticated mode.", connection.getAgentId());
        }

        usageMonitor.addConnection(connection.getAgentId());
    }

    public void removeConnection(Session session)
    {
        AgentConnection connection = connections.remove(session);
       // subscriptionManager.removeSession(state.getSession());
        usageMonitor.removeConnection(connection.getAgentId());
        logger.info("Removed connection: Agent ID = {}", connection.getAgentId());
    }


    public List<AgentConnection> getAllConnectionStates()
    {
        return new ArrayList<>(connections.values());
    }


    public AgentConnection getConnectionState(Session session)
    {
        return connections.get(session);
    }



    public void closeAllConnections(int statusCode, String reason)
    {
        for (AgentConnection connection : connections.values())
        {
            connection.getSession().close(statusCode, reason);
        }
    }
}
