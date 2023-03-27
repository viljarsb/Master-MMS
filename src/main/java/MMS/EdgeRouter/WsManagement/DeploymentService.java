package MMS.EdgeRouter.WsManagement;


import MMS.EdgeRouter.Exceptions.WsEndpointDeploymentException;
import MMS.EdgeRouter.ServiceRegistry.EndpointInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.io.ConnectionStatistics;
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


/**
 * The DeploymentService class provides methods for deploying and shutting down
 * WebSocket endpoints using Jetty. It is responsible for configuring the Jetty WebSocket
 * server and starting and stopping the server.
 */
public class DeploymentService
{
    private static final Logger logger = LogManager.getLogger(DeploymentService.class);
    private static final ConnectionHandler connectionHandler = ConnectionHandler.getInstance();


    /**
     * Deploys a WebSocket endpoint using the provided EndpointInfo configuration.
     * Configures and starts a Jetty WebSocket server.
     *
     * @param endpointInfo The configuration details for the WebSocket endpoint.
     * @return A Server instance representing the deployed WebSocket server.
     * @throws WsEndpointDeploymentException If an error occurs while starting the WebSocket server.
     */
    public static Server deployEndpoint(EndpointInfo endpointInfo) throws WsEndpointDeploymentException
    {
        logger.info("Deploying WebSocket endpoint at path: {}, port: {}", endpointInfo.getServicePath(), endpointInfo.getServicePort());

        String path = endpointInfo.getServicePath();
        int port = endpointInfo.getServicePort();
        int endpointMaxConnections = endpointInfo.getMaxConnections();

        Server server = new Server(new QueuedThreadPool(endpointMaxConnections));

        DoSFilter dosFilter = setupDOSFilter(endpointInfo);
        SslContextFactory sslContextFactory = endpointInfo.getTlsConfiguration().getTLSContextFactory();

        ServerConnector tlsConnector = new ServerConnector(server, sslContextFactory);
        tlsConnector.setPort(port);
        tlsConnector.setHost(endpointInfo.getAddress().getHostAddress());

        ConnectionStatistics connectionStatistics = new ConnectionStatistics();
        tlsConnector.addBean(connectionStatistics);

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
                factory.getPolicy().setIdleTimeout(600000);
            }
        };

        context.setHandler(handler); // Set the WebSocketHandler as the handler of the ServletContextHandler
        server.setHandler(context); // Set the ServletContextHandler as the server handler

        try
        {
            startServerWithRetry(server, 3);
        }


        catch (Exception ex)
        {
            logger.error("Error while starting WebSocket server: {}", ex.getMessage(), ex);
            throw new WsEndpointDeploymentException("Error while starting websocket server: " + ex.getMessage());
        }

        logger.info("WebSocket server deployed successfully");
        return server;
    }


    /**
     * Shuts down the provided WebSocket server instance.
     * Closes all active sessions and stops the server.
     *
     * @param server The Server instance to be shut down.
     */
    public static void shutdown(Server server)
    {
        logger.info("Shutting down WebSocket server at URI: {}", server.getURI());
        connectionHandler.getSessions(server.getURI())
                .forEach(session -> session.close(1000, "Edge Router Shutdown"));

        server.destroy();

        logger.info("WebSocket server at URI: {}, shutdown successfully", server.getURI());
    }


    /**
     * Attempts to start the provided WebSocket server with a specified number of retries.
     *
     * @param server     The Server instance to be started.
     * @param maxRetries The maximum number of retries for starting the server.
     * @throws Exception If starting the server fails after the specified number of retries.
     */
    private static void startServerWithRetry(Server server, int maxRetries) throws Exception
    {
        logger.info("Attempting to start WebSocket server with {} retries", maxRetries);
        Exception lastException = null;

        for (int i = 0; i < maxRetries; i++)
        {
            try
            {
                server.start();
                logger.info("Websocket server started successfully");
                return;
            }

            catch (Exception ex)
            {
                lastException = ex;
                logger.error("Error while starting websocket server (attempt {} of {}): {}", i + 1, maxRetries, ex.getMessage());
            }
        }

        throw lastException != null ? lastException : new Exception("Failed to start the websocket server");
    }


    /**
     * Configures a DoSFilter instance based on the provided EndpointInfo configuration.
     *
     * @param endpointInfo The configuration details for the WebSocket endpoint.
     * @return A DoSFilter instance configured according to the specified EndpointInfo.
     */
    private static DoSFilter setupDOSFilter(EndpointInfo endpointInfo)
    {
        logger.info("Setting up DoSFilter with EndpointInfo: {}", endpointInfo);
        DoSFilter dosFilter = new DoSFilter();
        dosFilter.setMaxRequestsPerSec(endpointInfo.getMaxRequestsPerSec());
        dosFilter.setDelayMs(endpointInfo.getDelayMs());
        dosFilter.setThrottleMs(endpointInfo.getThrottleMs());
        dosFilter.setTrackSessions(endpointInfo.isTrackSessions());
        dosFilter.setWhitelist(endpointInfo.getWhitelist());
        dosFilter.setTooManyCode(endpointInfo.getTooManyCode());
        return dosFilter;
    }
}
