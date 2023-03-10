package MMS.Agent.WebSocketEndpointManager;

import MMS.EncoderDecoder.MMTPDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;


@WebSocket
public class AgentEndpoint
{
    private static final Logger logger = LogManager.getLogger(AgentEndpoint.class);


    @OnWebSocketConnect
    public void onOpen(Session session)
    {
        logger.info("New connection: " + session.getRemoteAddress().getAddress().getHostAddress());
    }


    @OnWebSocketMessage
    public void onMessage(String message)
    {
        logger.info("Message received: " + message);
    }


    @OnWebSocketClose
    public void onClose(int statusCode, String reason)
    {
        logger.info("Connection closed: " + statusCode + " reason: " + reason);
    }


    @OnWebSocketError
    public void onError(Session session, Throwable throwable)
    {
        logger.error("Error: " + throwable.getMessage());
    }
}
