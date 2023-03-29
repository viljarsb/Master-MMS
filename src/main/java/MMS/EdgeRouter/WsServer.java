package MMS.EdgeRouter;

import MMS.EdgeRouter.Exceptions.WsEndpointDeploymentException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlets.DoSFilter;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

public class WsServer
{
    private static final Logger logger = LogManager.getLogger(WsServer.class);

    private final Server wsServer;
    private boolean isStarted;


    private WsServer(EdgeRouterConfig config)
    {
        wsServer = configureServer(config);
        logger.info("WebSocket server created");
    }


    public static WsServer create(EdgeRouterConfig config)
    {
        return new WsServer(config);
    }


    public void start() throws WsEndpointDeploymentException
    {
        if (isStarted)
        {
            throw new WsEndpointDeploymentException("WebSocket server already started");
        }

        int retriesLeft = 3;

        while (retriesLeft > 0)
        {
            try
            {
                wsServer.start();
                isStarted = true;
                logger.info("WebSocket server started");
                return;
            }
            catch (Exception ex)
            {
                retriesLeft--;

                if (retriesLeft == 0)
                {
                    throw new WsEndpointDeploymentException("Error starting WebSocket server after 3 retries: " + ex.getMessage(), ex.getCause());
                }
                else
                {
                    logger.error("Error starting WebSocket server, retrying in 5 seconds ({} retries left).. ", retriesLeft, ex);

                    try
                    {
                        Thread.sleep(5000);
                    }
                    catch (InterruptedException ignored)
                    {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }


    public void stop()
    {
        if (!isStarted)
        {
            return;
        }

        try
        {
            wsServer.stop();
            logger.info("WebSocket server stopped");
        }

        catch (Exception ex)
        {
            logger.error("Error stopping WebSocket server", ex);
        }

        finally
        {
            wsServer.destroy();
            logger.info("WebSocket server destroyed");
        }
    }


    public Server configureServer(EdgeRouterConfig config)
    {
        String path = config.getPath();
        int port = config.getPort();
        int endpointMaxConnections = config.getMaxConnections();

        Server server = new Server(new QueuedThreadPool(endpointMaxConnections));

        DoSFilter dosFilter = setupDOSFilter(config);
        SslContextFactory sslContextFactory = config.getTlsConfiguration().getTLSContextFactory();

        ServerConnector tlsConnector = new ServerConnector(server, sslContextFactory);
        tlsConnector.setPort(port);
        tlsConnector.setHost(config.getIp().getHostAddress());

        server.addConnector(tlsConnector);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath(path);

        FilterHolder dosFilterHolder = new FilterHolder(dosFilter);
        context.addFilter(dosFilterHolder, "/*", EnumSet.of(DispatcherType.REQUEST));

        WebSocketHandler handler = new WebSocketHandler()
        {
            @Override
            public void configure(WebSocketServletFactory factory)
            {
                factory.register(WsEndpoint.class);
                factory.getPolicy().setIdleTimeout(600_000); // use underscores for readability
            }
        };

        context.insertHandler(handler);
        server.setHandler(context);

        return server;
    }


    private DoSFilter setupDOSFilter(EdgeRouterConfig config)
    {
        DoSFilter dosFilter = new DoSFilter();
        dosFilter.setMaxRequestsPerSec(config.getMaxRequestsPerSec());
        dosFilter.setDelayMs(config.getDelayMs());
        dosFilter.setThrottleMs(config.getThrottleMs());
        dosFilter.setTrackSessions(config.isTrackSessions());
        dosFilter.setWhitelist(config.getWhitelist());
        dosFilter.setTooManyCode(config.getTooManyCode());
        return dosFilter;
    }
}