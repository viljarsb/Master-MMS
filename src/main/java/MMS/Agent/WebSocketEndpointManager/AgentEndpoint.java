package MMS.Agent.WebSocketEndpointManager;


import MMS.EdgeRouter.WebsocketServerManager.ConnectionHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import java.util.Arrays;

/**
 * The AgentEndpoint class extends WebSocketAdapter to handle WebSocket communication
 * with a remote edge router. It processes incoming messages and delegates handling
 * of binary messages to the provided MessageHandler.
 */
public class AgentEndpoint extends WebSocketAdapter
{
    private static final Logger logger = LogManager.getLogger(AgentEndpoint.class);
    private final MessageHandler messageHandler;
    private Session session;


    /**
     * Initializes an AgentEndpoint with the provided MessageHandler.
     *
     * @param messageHandler The MessageHandler responsible for processing incoming binary messages.
     */
    public AgentEndpoint(MessageHandler messageHandler)
    {
        this.messageHandler = messageHandler;
    }


    /**
     * Handles the connection event to the WebSocket.
     *
     * @param session The WebSocket session.
     */
    @Override
    public void onWebSocketConnect(Session session)
    {
        super.onWebSocketConnect(session);
        this.session = session;
        logger.info("Socket Connected: " + session.getRemoteAddress());
    }


    /**
     * Handles the receipt of a text message from the WebSocket, which should not occur.
     *
     * @param message The received text message.
     */
    @Override
    public void onWebSocketText(String message)
    {
        super.onWebSocketText(message);
        logger.info("Received TEXT message, this should not happen: " + message);
    }


    /**
     * Handles the receipt of a binary message from the WebSocket and forwards it to the MessageHandler.
     *
     * @param payload The received binary message payload.
     * @param offset  The starting position of the payload.
     * @param len     The length of the payload.
     */
    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len)
    {
        super.onWebSocketBinary(payload, offset, len);
        logger.info("Received BINARY message, forwarding to message handler");
        payload = Arrays.copyOfRange(payload, offset, offset + len);
        messageHandler.handle(payload);
    }


    /**
     * Handles the close event of the WebSocket.
     *
     * @param statusCode The status code for the closure.
     * @param reason     The reason for the closure.
     */
    @Override
    public void onWebSocketClose(int statusCode, String reason)
    {
        super.onWebSocketClose(statusCode, reason);
        logger.info("Socket Closed: [" + statusCode + "] " + reason);
    }


    /**
     * Handles any errors that occur on the WebSocket.
     *
     * @param cause The exception that caused the error.
     */
    @Override
    public void onWebSocketError(Throwable cause)
    {
        super.onWebSocketError(cause);
        logger.error("Socket Error: " + cause.getMessage(), cause);
        this.session.close(StatusCode.ABNORMAL, "Socket Error: " + cause.getMessage());
    }
}
