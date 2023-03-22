package MMS.EdgeRouter.WebsocketServerManager;

import MMS.EdgeRouter.EdgeRouterService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlets.DoSFilter;
import org.eclipse.jetty.servlets.QoSFilter;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

class ServerDeployer
{
    private static final Logger logger = LogManager.getLogger(ServerDeployer.class);
    private static ServerDeployer instance;
    private Server server;


    public static ServerDeployer getDeployer()
    {
        if (instance == null)
        {
            instance = new ServerDeployer();
        }
        return instance;
    }


    private ServerDeployer()
    {
        this.server = null;
    }

    public void deployServer(EdgeRouterService service) throws Exception {
        String path = service.getServicePath();
        int port = service.getServicePort();
        int endpointMaxConnections = service.getMaxConnections();

        server = new Server(new QueuedThreadPool(endpointMaxConnections));

        DoSFilter dosFilter = new DoSFilter();
        dosFilter.setMaxRequestsPerSec(service.getMaxRequestsPerSec());
        dosFilter.setDelayMs(service.getDelayMs());
        dosFilter.setThrottleMs(service.getThrottleMs());
        dosFilter.setTrackSessions(service.isTrackSessions());
        dosFilter.setWhitelist(service.getWhitelist());
        dosFilter.setTooManyCode(service.getTooManyCode());

        SslContextFactory sslContextFactory = TLSConfigurator.configureTLS();

        ServerConnector tlsConnector = new ServerConnector(server, sslContextFactory);
        tlsConnector.setPort(port);
        tlsConnector.setHost(service.getLocalAddress());

        server.addConnector(tlsConnector);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath(path);

        FilterHolder dosFilterHolder = new FilterHolder(dosFilter);
        context.addFilter(dosFilterHolder, "/*", EnumSet.of(DispatcherType.REQUEST));

        WebSocketHandler handler = new WebSocketHandler() {
            @Override
            public void configure(WebSocketServletFactory factory) {
                factory.register(EdgeRouterEndpoint.class);
                factory.getPolicy().setIdleTimeout(600000);
            }
        };

        context.setHandler(handler); // Set the WebSocketHandler as the handler of the ServletContextHandler
        server.setHandler(context); // Set the ServletContextHandler as the server handler

        server.start();
        logger.info("WebSocket server endpoint successfully deployed on URI: " + server.getURI());
    }


    public void undeployServer()
    {
        try
        {
            server.stop();
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

