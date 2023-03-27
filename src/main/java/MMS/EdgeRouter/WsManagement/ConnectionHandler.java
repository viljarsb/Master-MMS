package MMS.EdgeRouter.WsManagement;

import MMS.EdgeRouter.SubscriptionManager.SubscriptionManager;
import net.maritimeconnectivity.pki.PKIIdentity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@code ConnectionHandler} class is a singleton that manages WebSocket connections
 * and their associated {@code ConnectionState} objects. It provides methods for adding,
 * removing, and retrieving WebSocket sessions and their states.
 */
public class ConnectionHandler
{
    private static final Logger logger = LogManager.getLogger(ConnectionHandler.class);
    private static ConnectionHandler instance;

    private final ConcurrentHashMap<Session, ConnectionState> connections = new ConcurrentHashMap<>();
    private final SubscriptionManager subscriptionManager = SubscriptionManager.getInstance();


    /**
     * Constructs a new {@code ConnectionHandler} instance. This constructor is private
     * to ensure that only one instance of the class is created (singleton pattern).
     */
    private ConnectionHandler()
    {
        logger.info("Connection handler created");
    }


    /**
     * Returns the singleton instance of the {@code ConnectionHandler}. If no instance
     * exists, a new one is created.
     *
     * @return the singleton instance of {@code ConnectionHandler}
     */
    public synchronized static ConnectionHandler getInstance()
    {
        if (instance == null)
            instance = new ConnectionHandler();

        return instance;
    }


    /**
     * Adds a WebSocket session and its associated {@code ConnectionState} to the
     * collection of connections. This means that a new Agent has connected.
     *
     * @param session the WebSocket session to be added
     */
    public void addConnection(Session session)
    {
        ConnectionState state = new ConnectionState(session);
        connections.put(session, state);
        PKIIdentity identity = state.getIdentity();

        if (identity != null)
        {
            logger.info("Agent added: Agent ID = {} - connected in authenticated mode.\nCN: {}\n{}", state.getAgentId(), identity.getCn(), identity.getMrn());
            subscriptionManager.addDirectMessageSubscription(session);
        }

        else
        {
            logger.info("Agent added: Agent ID = {} - connected in unauthenticated mode.", state.getAgentId());
        }
    }


    /**
     * Removes a WebSocket session and its associated {@code ConnectionState} from the
     * collection of connections. This means that an Agent has disconnected.
     *
     * @param session the WebSocket session to be removed
     */
    public void removeConnection(Session session)
    {
        ConnectionState state = connections.remove(session);
        subscriptionManager.removeSession(state.getSession());
        logger.info("Removed connection: Agent ID = {}", state.getAgentId());
    }


    /**
     * Retrieves all WebSocket sessions connected to a specific URI (an endpoint).
     *
     * @param uri the URI for which to retrieve connected sessions
     * @return a list of WebSocket sessions connected to the given URI
     */
    public List<Session> getSessions(URI uri)
    {
        List<Session> sessions = new ArrayList<>();
        for (ConnectionState connectionStates : connections.values())
        {
            if (connectionStates.getConnectionURI().equals(uri))
            {
                sessions.add(connectionStates.getSession());
            }
        }

        return sessions;
    }


    /**
     * Retrieves all {@code ConnectionState} objects associated with all WebSocket sessions.
     *
     * @return a list of all {@code ConnectionState} objects
     */
    public List<ConnectionState> getAllConnectionStates()
    {
        return new ArrayList<>(connections.values());
    }


    /**
     * Retrieves the {@code ConnectionState} associated with a given WebSocket session.
     *
     * @param session the WebSocket session for which to retrieve the {@code ConnectionState}
     * @return the {@code ConnectionState} associated with the session, or null if not found
     */
    public ConnectionState getConnectionState(Session session)
    {
        return connections.get(session);
    }
}
