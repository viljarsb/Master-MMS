package MMS.Agent.WebSocketEndpointManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;


import javax.net.ssl.SSLContext;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

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


    public void connectAnonymously(String uri) throws URISyntaxException, DeploymentException, IOException
    {
        try
        {
            SSLContext tlsContext = TLSContextManager.getTLSContext();
            SslContextFactory sslContextFactory = new SslContextFactory.Client(false);
            sslContextFactory.setSslContext(tlsContext);
            HttpClient httpClient = new HttpClient(sslContextFactory);
            httpClient.start();

            WebSocketClient client = new WebSocketClient(sslContextFactory);
            client.start();
            URI destination = new URI(uri);
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            AgentEndpoint agentEndpoint = new AgentEndpoint();
            client.connect(agentEndpoint, destination, request);
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


    public void connectAuthenticated(String uri) throws URISyntaxException, DeploymentException, IOException
    {
        try
        {

            SSLContext tlsContext = TLSContextManager.getTLSContext();
            SslEngineConfigurator sslEngineConfigurator = new SslEngineConfigurator(tlsContext, true, true, true);
            clientManager.getProperties().put(ClientProperties.SSL_ENGINE_CONFIGURATOR, sslEngineConfigurator);
            session = clientManager.connectToServer(AgentEndpoint.class, new URI(uri));
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
        catch (CertificateException e)
        {
            throw new RuntimeException(e);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
        catch (KeyStoreException e)
        {
            throw new RuntimeException(e);
        }
        catch (KeyManagementException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void disconnect() throws IOException, IllegalStateException
    {
        if(session == null)
            throw new IllegalStateException("No session to disconnect");

        session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Disconnecting"));
        session = null;
    }


}
