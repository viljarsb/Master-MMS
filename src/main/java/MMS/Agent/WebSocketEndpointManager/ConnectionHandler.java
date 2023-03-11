package MMS.Agent.WebSocketEndpointManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;


import javax.net.ssl.SSLContext;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class ConnectionHandler
{
    private static final Logger logger = LogManager.getLogger(ConnectionHandler.class);
    private WebSocketClient client;
    private Session session;
    private static ConnectionHandler connectionHandler;


    private ConnectionHandler()
    {
        client = new WebSocketClient();
    }


    public synchronized static ConnectionHandler getConnectionHandler()
    {
        if (connectionHandler == null)
        {
            connectionHandler = new ConnectionHandler();
        }

        return connectionHandler;
    }


    public void connectAnonymously(String uri) throws URISyntaxException, DeploymentException, IOException, TimeoutException
    {
        try
        {
            SSLContext tlsContext = TLSContextManager.getTLSContext();

            SslContextFactory sslContextFactory = new SslContextFactory.Client(false);
            sslContextFactory.setSslContext(tlsContext);

            HttpClient httpClient = new HttpClient(sslContextFactory);
            httpClient.start();

            WebSocketClient client = new WebSocketClient(httpClient);
            client.start();

            URI destination = new URI(uri);

            ClientUpgradeRequest request = new ClientUpgradeRequest();
            request.setHeader("Sec-WebSocket-Protocol", "MMTP");
            request.setHeader("Authentication-wanted", "false");

            AgentEndpoint agentEndpoint = new AgentEndpoint();
            Future<Session> sessionFuture = client.connect(agentEndpoint, destination, request);

            session = sessionFuture.get(5, TimeUnit.SECONDS);
        }

        catch (TimeoutException ex)
        {
            logger.error("Connection request timed out: " + uri);
            throw ex;
        }

        catch (URISyntaxException ex)
        {
            logger.error("Connection request failed, invalid URI: " + uri);
            throw ex;
        }

        catch (DeploymentException ex)
        {
            logger.error("Connection request failed, deployment exception: " + uri);
            throw ex;
        }

        catch (IOException ex)
        {
            logger.error("Connection request failed, IO exception: " + uri);
            throw ex;
        }

        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }


    public void connectAuthenticated(String uri)
    {

    }


    public void disconnect() throws IllegalStateException
    {
        if (session == null)
            throw new IllegalStateException("No session to disconnect");

        session.close(StatusCode.NORMAL, "Agent disconnected");
        session = null;
    }
}
