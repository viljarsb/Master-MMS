package MMS.EdgeRouter.RouterWebSocketManager;

import MMS.EncoderDecoder.MMTPDecoder;
import MMS.EncoderDecoder.MMTPEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/edge-router", encoders = {MMTPEncoder.class}, decoders = {MMTPDecoder.class})
public class EdgeRouterEndpoint
{
    private static final Logger logger = LogManager.getLogger(EdgeRouterEndpoint.class);
    private final PingHandler pingManager;

    public EdgeRouterEndpoint()
    {
        this.pingManager = new PingHandler();
    }


    @OnOpen
    public void onOpen(Session session)
    {
        pingManager.addConnection(session);
        logger.info("New connection: " + session.getId());
    }


    @OnMessage
    public void onMessage(Session session, String message)
    {

    }


    @OnClose
    public void onClose(Session session, CloseReason closeReason)
    {
        pingManager.removeConnection(session);
        logger.info("Connection closed: " + session.getId() + " reason: " + closeReason.getReasonPhrase());
    }


    @OnError
    public void onError(Session session, Throwable t)
    {

    }


    @OnMessage
    public void onMessage(Session session, PongMessage pongMessage)
    {
        pingManager.registerPong(session);
        logger.debug("Pong received from " + session.getId());
    }
}
