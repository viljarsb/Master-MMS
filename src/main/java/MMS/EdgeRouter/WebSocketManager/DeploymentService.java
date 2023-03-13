package MMS.EdgeRouter.WebSocketManager;

import MMS.EdgeRouter.Configuration.TLSContextManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;


public class DeploymentService
{
    private static final Logger logger = LogManager.getLogger(DeploymentService.class);
    private static DeploymentService instance;
    private final HashMap<URI, Server> deployedEndpoints;
    private final ConnectionHandler connectionHandler;
    private final WebSocketServerRegistry serverRegistry;


    private DeploymentService()
    {
        this.deployedEndpoints = new HashMap<>();
        this.connectionHandler = ConnectionHandler.getHandler();
        this.serverRegistry = WebSocketServerRegistry.getRegistry();
    }


    public static DeploymentService getService()
    {
        if (instance == null)
        {
            instance = new DeploymentService();
        }

        return instance;
    }


    public Server deployEndpoint(int endpointPort, String endpointPath, Integer endpointMaxConnections) throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException, KeyManagementException, UnrecoverableKeyException
    {
        Server server;

        if (endpointMaxConnections != null)
        {
            server = new Server(new QueuedThreadPool(endpointMaxConnections));
        }

        else
        {
            server = new Server();
        }

        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server(); // only for testing
        sslContextFactory.setSslContext(TLSContextManager.getTLSContext());
        sslContextFactory.setWantClientAuth(true);

        ServerConnector tlsConnector = new ServerConnector(server, sslContextFactory);
        tlsConnector.setPort(endpointPort);

        server.addConnector(tlsConnector);

        ContextHandler context = new ContextHandler();
        context.setContextPath(endpointPath);

        WebSocketHandler handler = new WebSocketHandler()
        {
            @Override
            public void configure(WebSocketServletFactory factory)
            {
                factory.register(EdgeRouterEndpoint.class);
                factory.getPolicy().setIdleTimeout(600000);
            }
        };

        handler.setHandler(context);
        server.setHandler(handler);

        try
        {
            server.start();

            if (deployedEndpoints.containsKey(server.getURI()))
            {
                throw new IllegalStateException("Endpoint already deployed");
            }

            deployedEndpoints.put(server.getURI(), server);
            logger.info("Endpoint deployed at: " + server.getURI());
            return server;
        }

        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }


    public void undeployEndpoint(Server server) throws Exception
    {
        if (!deployedEndpoints.containsKey(URI))
        {
            throw new IllegalStateException("Endpoint not deployed");
        }

        Server server = deployedEndpoints.get(URI);
            connectionHandler.removeServer(URI);
        server.stop();
        deployedEndpoints.remove(URI);
    }
}
