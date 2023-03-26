package MMS.EdgeRouter.WsManagement;

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
    private final ConcurrentHashMap<Session, ConnectionState> connections;
    private static ConnectionHandler instance;


    /**
     * Constructs a new {@code ConnectionHandler} instance. This constructor is private
     * to ensure that only one instance of the class is created (singleton pattern).
     */
    private ConnectionHandler()
    {
        connections = new ConcurrentHashMap<>();
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
        {
            instance = new ConnectionHandler();
        }

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
    }


    /**
     * Removes a WebSocket session and its associated {@code ConnectionState} from the
     * collection of connections. This means that an Agent has disconnected.
     *
     * @param session the WebSocket session to be removed
     */
    public void removeConnection(Session session)
    {
        connections.remove(session);
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
