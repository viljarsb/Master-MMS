package MMS.EdgeRouter.WsManagement;

import MMS.EdgeRouter.ServiceRegistry.EndpointInfo;
import MMS.EdgeRouter.Exceptions.WsEndpointDeploymentException;
import MMS.EdgeRouter.Exceptions.WsEndpointUndeploymentException;
import MMS.EdgeRouter.ServiceRegistry.TLSConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.websocket.api.Session;

import java.util.HashMap;
import java.util.List;

/**
 * The {@code WsEndpointManager} class is responsible for deploying, managing, and shutting down WebSocket endpoints.
 */
public class WsEndpointManager
{
    private static final Logger logger = LogManager.getLogger(WsEndpointManager.class);
    private static WsEndpointManager instance;

    private final HashMap<String, Server> deployedEndpoints;
    private final DeploymentService deploymentService;


    /**
     * Constructs a new {@code WsEndpointManager} instance.
     */
    public WsEndpointManager()
    {
        this.deployedEndpoints = new HashMap<>();
        this.deploymentService = new DeploymentService();
    }


    /**
     * Deploys a WebSocket endpoint using the provided {@code EndpointInfo}.
     *
     * @param endpointInfo the {@code EndpointInfo} object containing the configuration details for the endpoint
     * @throws WsEndpointDeploymentException if the endpoint deployment fails or if the endpoint is already deployed
     */
    public void deployEndpoint(EndpointInfo endpointInfo) throws WsEndpointDeploymentException
    {
        String name = endpointInfo.getServiceName();

        if (deployedEndpoints.containsKey(name))
        {
            logger.error("Endpoint already deployed: " + name);
            throw new WsEndpointDeploymentException("Endpoint already deployed: " + name);
        }

        TLSConfiguration tlsConfiguration = null;

        try
        {
            Server server = deploymentService.deployEndpoint(endpointInfo);
            deployedEndpoints.put(name, server);
        }

        catch (Exception ex)
        {
            logger.error("Could not deploy endpoint: " + ex.getMessage());
            throw new WsEndpointDeploymentException("Failed to deploy endpoint: " + name, ex.getCause());
        }
    }


    /**
     * Shuts down the WebSocket endpoint with the specified service name.
     *
     * @param serviceName the name of the WebSocket endpoint to be shut down
     * @throws WsEndpointUndeploymentException if the endpoint shutdown fails
     */
    public void shutdown(String serviceName) throws WsEndpointUndeploymentException
    {
        Server server = deployedEndpoints.get(serviceName);

        if (server != null)
        {
            deploymentService.shutdown(server);
            deployedEndpoints.remove(serviceName);
        }

        else
        {
            logger.error("Failed to shutdown endpoint: " + serviceName);
            throw new WsEndpointUndeploymentException("Failed to shutdown endpoint: " + serviceName);
        }
    }


    /**
     * Returns the number of active connections for the specified WebSocket endpoint.
     *
     * @param serviceName the name of the WebSocket endpoint
     * @return the number of active connections for the specified endpoint
     */
    public int getConnections(String serviceName)
    {
        Server server = deployedEndpoints.get(serviceName);
        List<Session> sessions = ConnectionHandler.getInstance().getSessions(server.getURI());
        return sessions.size();
    }


    /**
     * Returns the number of active connections for all deployed WebSocket endpoints.
     *
     *
     * @return the total number of active connections for all deployed endpoints
     */
    public int getTotalConnections()
    {
        int totalConnections = 0;

        for (Server server : deployedEndpoints.values())
        {
            totalConnections += getConnections(server.getURI().toString());
        }

        return totalConnections;
    }

}
