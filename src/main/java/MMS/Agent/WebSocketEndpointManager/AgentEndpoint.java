package MMS.Agent.WebSocketEndpointManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;


public class AgentEndpoint extends WebSocketAdapter
{
    private static final Logger logger = LogManager.getLogger(AgentEndpoint.class);


    @Override
    public void onWebSocketConnect(Session session)
    {
        super.onWebSocketConnect(session);
        logger.info("Socket Connected: " + session);
    }


    @Override
    public void onWebSocketText(String message)
    {
        super.onWebSocketText(message);
        logger.info("Received TEXT message: " + message);
    }


    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len)
    {
        super.onWebSocketBinary(payload, offset, len);
        logger.info("Received BINARY message: " + new String(payload, offset, len));
    }


    @Override
    public void onWebSocketClose(int statusCode, String reason)
    {
        super.onWebSocketClose(statusCode, reason);
        logger.info("Socket Closed: [" + statusCode + "] " + reason);
    }


    @Override
    public void onWebSocketError(Throwable cause)
    {
        super.onWebSocketError(cause);
        logger.error("Socket Error: " + cause.getMessage(), cause);
    }
}
