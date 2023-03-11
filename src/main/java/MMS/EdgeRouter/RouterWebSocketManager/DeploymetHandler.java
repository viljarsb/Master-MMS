package MMS.EdgeRouter.RouterWebSocketManager;

import MMS.EdgeRouter.Configuration.Config;
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
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;


public class DeploymetHandler
{
    private static final Logger logger = LogManager.getLogger(DeploymetHandler.class);
    private static final HashMap<Integer, Server> deployedEndpoints = new HashMap<>();


    public void deployEndpoint(int endpointPort, String endpointPath, Integer endpointMaxConnections) throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException, KeyManagementException, UnrecoverableKeyException
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

        server.setHandler(handler);

        try
        {
            server.start();
            deployedEndpoints.put(endpointPort, server);
        }

        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }


    public void undeployEndpoint(int endpointPort)
    {
        Server server = deployedEndpoints.get(endpointPort);
        try
        {
            server.stop();
            deployedEndpoints.remove(endpointPort);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }


    public void undeployAllEndpoints()
    {
        for (Server server : deployedEndpoints.values())
        {
            try
            {
                server.stop();
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        deployedEndpoints.clear();
    }


}
