package MMS.EdgeRouter.WsManagement;


import MMS.EdgeRouter.ServiceRegistry.EndpointInfo;
import MMS.EdgeRouter.Exceptions.WsEndpointUndeploymentException;
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
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;


/**
 * The DeploymentService class provides methods for deploying and shutting down
 * WebSocket endpoints using Jetty. It is responsible for configuring the Jetty WebSocket
 * server and starting and stopping the server.
 */
public class DeploymentService
{
    private static final Logger logger = LogManager.getLogger(DeploymentService.class);
    private final ConnectionHandler connectionHandler;


    /**
     * Constructs a new DeploymentService instance.
     */
    public DeploymentService()
    {
        this.connectionHandler = ConnectionHandler.getInstance();
    }


    /**
     * Deploys a WebSocket endpoint using the given EndpointInfo.
     *
     * @param endpointInfo the configuration information for the endpoint
     * @return the deployed Jetty Server instance
     * @throws Exception if an error occurs during deployment
     */
    public Server deployEndpoint(EndpointInfo endpointInfo) throws Exception
    {
        String path = endpointInfo.getServicePath();
        int port = endpointInfo.getServicePort();
        int endpointMaxConnections = endpointInfo.getMaxConnections();

        Server server = new Server(new QueuedThreadPool(endpointMaxConnections));

        DoSFilter dosFilter = new DoSFilter();
        dosFilter.setMaxRequestsPerSec(endpointInfo.getMaxRequestsPerSec());
        dosFilter.setDelayMs(endpointInfo.getDelayMs());
        dosFilter.setThrottleMs(endpointInfo.getThrottleMs());
        dosFilter.setTrackSessions(endpointInfo.isTrackSessions());
        dosFilter.setWhitelist(endpointInfo.getWhitelist());
        dosFilter.setTooManyCode(endpointInfo.getTooManyCode());

        SslContextFactory sslContextFactory = endpointInfo.getTlsConfiguration().getTLSContextFactory();

        ServerConnector tlsConnector = new ServerConnector(server, sslContextFactory);
        tlsConnector.setPort(port);
        tlsConnector.setHost(endpointInfo.getAddress());

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

        server.start();
        return server;
    }


    /**
     * Shuts down all WebSocket endpoints in the provided list of Jetty Server instances.
     *
     * @param servers the list of Jetty Server instances to be shut down
     * @throws WsEndpointUndeploymentException if an error occurs during the shutdown process
     */
    public void shutdownAll(List<Server> servers) throws WsEndpointUndeploymentException
    {
        List<Server> serversToStop = new ArrayList<>(servers);
        for (Server server : servers)
        {
            try
            {
                connectionHandler.getSessions(server.getURI())
                        .forEach(session -> session.close(1000, "Edge Router Shutdown"));
                server.stop();

                serversToStop.remove(server);
            }

            catch (Exception e)
            {
                logger.error("Error while stopping server: " + e.getMessage());
            }

            if (!serversToStop.isEmpty())
                throw new WsEndpointUndeploymentException("Error while stopping servers", serversToStop);
        }
    }


    /**
     * Shuts down a single WebSocket endpoint represented by the provided Jetty Server instance.
     *
     * @param server the Jetty Server instance to be shut down
     * @throws WsEndpointUndeploymentException if an error occurs during the shutdown process
     */
    public void shutdown(Server server) throws WsEndpointUndeploymentException
    {
        try
        {
            connectionHandler.getSessions(server.getURI())
                    .forEach(session -> session.close(1000, "Edge Router Shutdown"));
            server.stop();
        }

        catch (Exception e)
        {
            logger.error("Error while stopping server: " + e.getMessage());
            throw new WsEndpointUndeploymentException("Error while stopping server: " + e.getMessage());
        }

    }
}
