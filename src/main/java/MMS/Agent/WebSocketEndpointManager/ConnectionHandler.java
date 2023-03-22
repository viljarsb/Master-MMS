package MMS.Agent.WebSocketEndpointManager;

import MMS.Agent.Exceptions.ConnectException;
import MMS.Agent.Exceptions.DisconnectException;
import MMS.Agent.AgentCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * The ConnectionHandler class provides functionality to establish secure WebSocket connections
 * to a specified edge router, using anonymous or mutual TLS authentication.
 */
public class ConnectionHandler
{
    private static final Logger logger = LogManager.getLogger(ConnectionHandler.class);

    private final MessageHandler messageHandler;
    private Session session;


    /**
     * Initializes a ConnectionHandler with the provided callback.
     *
     * @param callback The message callback that will be used by the MessageHandler.
     */
    public ConnectionHandler(AgentCallback callback)
    {
        logger.info("ConnectionHandler initialized");
        this.messageHandler = new MessageHandler(callback);
    }


    /**
     * Connects to the specified edge router using anonymous TLS authentication.
     *
     * @param uri The WebSocket URI of the edge router to connect to.
     * @throws ConnectException If an error occurs while connecting to the edge router.
     */
    public void connectAnonymously(String uri, SslContextFactory tlsContext) throws ConnectException
    {
        try
        {
            connect(uri, tlsContext);
        }

        catch (Exception ex)
        {
            String message = "An error occurred while connecting to the edge router in anonymous mode";
            logger.error(message, ex);
            throw new ConnectException(message, ex);
        }
    }


    /**
     * Connects to the specified edge router using mutual TLS authentication.
     *
     * @param uri The WebSocket URI of the edge router to connect to.
     * @throws ConnectException If an error occurs while connecting to the edge router.
     */
    public void connectAuthenticated(String uri, SslContextFactory tlsContext) throws ConnectException
    {
        try
        {
            connect(uri, tlsContext);
        }

        catch (Exception ex)
        {
            String message = "An error occurred while connecting to the edge router in authenticated mode";
            logger.info(message, ex);
            throw new ConnectException(message, ex);
        }
    }


    /**
     * Establishes a WebSocket connection to the specified edge router using the provided SSL context factory.
     *
     * @param uri               The WebSocket URI of the edge router to connect to.
     * @param sslContextFactory The SSL context factory used to configure the secure connection.
     * @throws ConnectException If an error occurs while connecting to the edge router.
     */
    private void connect(String uri, SslContextFactory sslContextFactory) throws ConnectException
    {
        try
        {
            HttpClient httpClient = new HttpClient(sslContextFactory);
            httpClient.start();

            WebSocketClient client = new WebSocketClient(httpClient);
            client.start();

            URI destination = new URI(uri);

            ClientUpgradeRequest request = new ClientUpgradeRequest();
            request.setHeader("Sec-WebSocket-Protocol", "MMTP/1.0");

            AgentEndpoint agentEndpoint = new AgentEndpoint(messageHandler);

            Future<Session> sessionFuture = client.connect(agentEndpoint, destination, request);

            session = sessionFuture.get(5, TimeUnit.SECONDS);
        }

        catch (Exception ex)
        {
            throw new ConnectException(ex);
        }
    }


    /**
     * Disconnects the connection to the edge router.
     *
     * @throws DisconnectException If the agent is not connected to an edge router.
     */
    public void disconnect() throws DisconnectException
    {
        if (session == null)
            throw new DisconnectException("Agent is not connected to a edge router");

        session.close(StatusCode.NORMAL, "Agent is shutting down");
        session = null;
    }


    /**
     * Checks if the connection to the edge router is open.
     *
     * @return true if the connection is open, false otherwise.
     */
    public boolean isConnected()
    {
        return session.isOpen();
    }


    /**
     * Sends a binary message to the connected edge router.
     *
     * @param message The message to send, should be a valid MMTP message.
     * @throws IOException If an error occurs while sending the message.
     */
    public void sendMessage(byte[] message) throws IOException
    {
        session.getRemote().sendBytes(ByteBuffer.wrap(message));
    }
}
