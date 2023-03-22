package MMS.EdgeRouter.WebsocketServerManager;

import MMS.EdgeRouter.Config;
import net.maritimeconnectivity.pki.PKIIdentity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;

class EdgeRouterEndpoint extends WebSocketAdapter
{
    private static final Logger logger = LogManager.getLogger(EdgeRouterEndpoint.class);

    private final MessageHandler messageHandler;
    private final ConnectionHandler connectionHandler;

    private Session session;
    private String connectionID;


    public EdgeRouterEndpoint()
    {
        super();
        this.messageHandler = MessageHandler.getHandler();
        this.connectionHandler = ConnectionHandler.getHandler();
        this.session = null;
        this.connectionID = null;
    }


    @Override
    public void onWebSocketConnect(Session session)
    {
        super.onWebSocketConnect(session);
        this.session = session;
        this.connectionID = UUID.randomUUID().toString();

        connectionHandler.addConnection(connectionID, session);
        ConnectionState state = connectionHandler.getConnectionState(connectionID);

        PKIIdentity identity = state.getIdentity();

        if (!(identity == null))
        {
            String remoteAddress = session.getRemoteAddress().toString();
            String dn = identity.getDn();
            String cn = identity.getCn();
            String mrn = identity.getMrn();

            String logMsg = String.format(
                    "Connection established for authenticated user:\n"
                            + "Common Name: %s,\n"
                            + "Distinguished Name: %s,\n"
                            + "MRN: %s,\n"
                            + "Remote Address: %s", cn, dn, mrn, remoteAddress);

            logger.info(logMsg);
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
       // messageHandler.handleMessage(message, connectionID);
        logger.info("Received message: " + message);
    }


    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len)
    {
        super.onWebSocketBinary(payload, offset, len);
        messageHandler.handleMessage(payload, offset, len, connectionID);
        logger.info("Received binary message");
    }


    @Override
    public void onWebSocketClose(int statusCode, String reason)
    {
        super.onWebSocketClose(statusCode, reason);
        connectionHandler.removeConnection(connectionID);
        logger.info("Connection closed: " + statusCode + " " + reason);
    }


    @Override
    public void onWebSocketError(Throwable cause)
    {
        super.onWebSocketError(cause);
        connectionHandler.closeConnection(connectionID, StatusCode.SERVER_ERROR, cause.getMessage());
        logger.error("Connection error: " + cause.getMessage());
    }


}
