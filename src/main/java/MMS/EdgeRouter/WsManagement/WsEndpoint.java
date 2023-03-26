package MMS.EdgeRouter.WsManagement;

import net.maritimeconnectivity.pki.PKIIdentity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import java.util.UUID;


/**
 * The {@code WsEndpoint} class extends WebSocketAdapter and provides functionality for
 * handling WebSocket connections, specifically for binary messages that conform
 * to the MMTP specification. It adds the connection to the ConnectionHandler,
 * and handles incoming messages by forwarding them to the MessageHandler.
 */
public class WsEndpoint extends WebSocketAdapter
{
    private static final Logger logger = LogManager.getLogger(WsEndpoint.class);
    private static final ConnectionHandler connectionHandler = ConnectionHandler.getInstance();
    private static final MessageHandler messageHandler = new MessageHandler();
    private final String endpointURI;


    public WsEndpoint(String endpointURI)
    {
        super();
        this.endpointURI = endpointURI;
    }


    /**
     * Called when a WebSocket connection is established,
     * that is, when the Agent has successfully connected to the endpoint.
     *
     * @param session the WebSocket session that has been connected
     */
    @Override
    public void onWebSocketConnect(Session session)
    {
        super.onWebSocketConnect(session);

        connectionHandler.addConnection(session);
        ConnectionState state = connectionHandler.getConnectionState(session);

        PKIIdentity identity = state.getIdentity();

        if (identity != null)
            logger.info("WebSocket connection established from " + state.getRemoteAddress() + " to " + endpointURI + " for authenticated user: " + identity.getCn());
        else
            logger.info("WebSocket connection established from " + state.getRemoteAddress() + " to " + endpointURI + " for unauthenticated user");
    }


    /**
     * Called when a text message is received. This endpoint only accepts binary
     * messages, so text messages result in an error and the session is closed.
     *
     * @param message the received text message
     */
    @Override
    public void onWebSocketText(String message)
    {
        super.onWebSocketText(message);

        logger.error("Received text message, but this endpoint only accepts binary messages");
        getSession().close(StatusCode.PROTOCOL, "Message must be a valid binary MMTP message");
    }


    /**
     * Called when a binary message is received. The message is passed to the
     * MessageHandler for further processing.
     *
     * @param payload the payload of the received binary message
     * @param offset  the starting offset in the payload
     * @param len     the length of the message
     */
    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len)
    {
        super.onWebSocketBinary(payload, offset, len);

        messageHandler.handleMessage(payload, offset, len, getSession());
    }


    /**
     * Called when a WebSocket connection is closed. The connection is removed
     * from the ConnectionHandler.
     *
     * @param statusCode the status code of the close event
     * @param reason     the reason for the connection closure
     */
    @Override
    public void onWebSocketClose(int statusCode, String reason)
    {
        super.onWebSocketClose(statusCode, reason);

        logger.info("Connection closed with status code " + statusCode + " and reason: " + reason);
        connectionHandler.removeConnection(getSession());
    }


    /**
     * Called when a WebSocket error occurs. Logs the error message.
     * Does really nothing else then the default implementation,
     * as it handles most of the errors, message format errors are
     * handled by the MessageHandler.
     *
     * @param cause the exception that caused the error
     */
    @Override
    public void onWebSocketError(Throwable cause)
    {
        super.onWebSocketError(cause);

        logger.error("WebSocket error: " + cause.getMessage());
    }
}
