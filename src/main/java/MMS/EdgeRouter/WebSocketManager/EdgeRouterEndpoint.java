package MMS.EdgeRouter.WebSocketManager;

import net.maritimeconnectivity.pki.PKIIdentity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import java.util.UUID;


public class EdgeRouterEndpoint extends WebSocketAdapter
{
    private static final Logger logger = LogManager.getLogger(EdgeRouterEndpoint.class);
    private Session session;
    private UUID uuid;
    private ConnectionHandler connectionHandler;

    public EdgeRouterEndpoint()
    {
        super();
        this.connectionHandler = ConnectionHandler.getHandler();
        this.uuid = UUID.randomUUID();
    }


    @Override
    public void onWebSocketConnect(Session session)
    {
        super.onWebSocketConnect(session);
        this.session = session;
        connectionHandler.registerClient(session);

        SessionState state = connectionHandler.getClientState(session);

        if(state.isAuthenticated())
        {
            PKIIdentity identity = state.getIdentity();
            String remoteAddress = session.getRemoteAddress().toString();
            String fName = identity.getFirstName();
            String lName = identity.getLastName();
            String cn = identity.getCn();
            String mrn = identity.getMrn();

            String logMessage = String.format(
                    "Connection established for authenticated user:\n"
                            + "First Name: %s,\n"
                            + "Last Name: %s,\n"
                            + "Common Name: %s,\n"
                            + "MRN: %s,\n"
                            + "Remote Address: %s",
                    fName, lName, cn, mrn, remoteAddress);

            logger.info(logMessage);
        }

        else
        {
            String remoteAddress = session.getRemoteAddress().toString();
            String logMessage = String.format(
                    "Connection established for unauthenticated user:\n"
                            + "Remote Address: %s",
                    remoteAddress);
            logger.info(logMessage);
        }
    }


    @Override
    public void onWebSocketText(String message)
    {
        super.onWebSocketText(message);
        logger.info("Received message: " + message);
    }


    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len)
    {
        super.onWebSocketBinary(payload, offset, len);
        logger.info("Received binary message");
    }


    @Override
    public void onWebSocketClose(int statusCode, String reason)
    {
        super.onWebSocketClose(statusCode, reason);
        logger.info("Connection closed: " + statusCode + " " + reason);
    }


    @Override
    public void onWebSocketError(Throwable cause)
    {
        super.onWebSocketError(cause);
        logger.error("Connection error: " + cause.getMessage());
    }

}
