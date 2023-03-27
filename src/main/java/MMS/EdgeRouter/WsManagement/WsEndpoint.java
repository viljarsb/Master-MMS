package MMS.EdgeRouter.WsManagement;

import MMS.EdgeRouter.ThreadPoolService;
import net.maritimeconnectivity.pki.PKIIdentity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;



/**
 * The {@code WsEndpoint} class extends WebSocketAdapter and provides functionality for
 * handling WebSocket events, such as connections and incoming messages.
 * <p>
 * It adds and removes sessions from the {@code ConnectionHandler} as they are established
 * and terminated. It also handles incoming messages by passing them to the {@code MessageHandler}.
 * <p>
 * Every callback function runs asynchronously using a thread pool to avoid blocking the thread,
 * and to allow the ability to handle multiple events at the same time.
 */
public class WsEndpoint extends WebSocketAdapter
{
    private static final Logger logger = LogManager.getLogger(WsEndpoint.class);
    private static final ConnectionHandler connectionHandler = ConnectionHandler.getInstance();
    private static final MessageHandler messageHandler = MessageHandler.getInstance();


    /**
     * Called when a WebSocket connection is established,
     * that is, when the Agent has successfully connected to the endpoint.
     *
     * @param session the WebSocket session that has been connected
     */
    @Override
    public void onWebSocketConnect(Session session)
    {
        ThreadPoolService.executeAsync(() ->
        {
            super.onWebSocketConnect(session);
            logger.info("WebSocket connection established with: {} to {}", session.getRemoteAddress(), session.getUpgradeRequest().getRequestURI());
            connectionHandler.addConnection(session);
        });
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
        ThreadPoolService.executeAsync(() ->
        {
            super.onWebSocketText(message);
            logger.warn("Received text message from: {} to {}. Closing connection", getSession().getRemoteAddress(), getSession().getUpgradeRequest().getRequestURI());
            getSession().close(StatusCode.PROTOCOL, "Text messages are not supported");
        });
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
        ThreadPoolService.executeAsync(() ->
        {
            super.onWebSocketBinary(payload, offset, len);
            messageHandler.handleMessage(payload, offset, len, getSession());
        });
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
        ThreadPoolService.executeAsync(() ->
        {
            super.onWebSocketClose(statusCode, reason);
            logger.info("WebSocket connection closed with: {} to {}\nStatus code: {}, Reason {}", getSession().getRemoteAddress(), getSession().getUpgradeRequest().getRequestURI(), statusCode, reason);
            connectionHandler.removeConnection(getSession());
        });
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
        ThreadPoolService.executeAsync(() ->
        {
            super.onWebSocketError(cause);
            logger.warn("A WebSocket error occurred in the connetcion with: {} to {}", getSession().getRemoteAddress(), getSession().getUpgradeRequest().getRequestURI());
        });
    }
}
