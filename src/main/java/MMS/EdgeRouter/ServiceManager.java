package MMS.EdgeRouter;

import MMS.EdgeRouter.Exceptions.ServiceRegistrationException;
import MMS.EdgeRouter.Exceptions.WsEndpointUndeploymentException;
import MMS.EdgeRouter.ServiceBroadcast.ServiceBroadcastManager;
import MMS.EdgeRouter.ServiceRegistry.EndpointInfo;
import MMS.EdgeRouter.ServiceRegistry.EndpointRegistry;
import MMS.EdgeRouter.WsManagement.WsEndpointManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * The {@code ServiceManager} class is responsible for managing the lifecycle of services.
 * It handles adding, removing, and listing services, as well as managing connections.
 */
public class ServiceManager
{
    private static final Logger logger = LogManager.getLogger(ServiceManager.class);
    private static ServiceManager instance;

    private final EndpointRegistry endpointRegistry;
    private final ServiceBroadcastManager serviceBroadcastManager;
    private final WsEndpointManager wsEndpointManager;

    /**
     * Constructs a new {@code ServiceManager} instance.
     */
    private ServiceManager()
    {
        this.endpointRegistry = new EndpointRegistry();
        this.serviceBroadcastManager = new ServiceBroadcastManager();
        this.wsEndpointManager = new WsEndpointManager();
    }

    /**
     * Retrieves the singleton instance of the {@code ServiceManager} class.
     *
     * @return the singleton instance of the {@code ServiceManager} class
     */
    public static ServiceManager getServiceManager()
    {
        if (instance == null)
        {
            instance = new ServiceManager();
            logger.info("Service manager created");
        }

        return instance;
    }



    /**
     * Adds a new service using the provided {@code EndpointInfo}.
     *
     * @param endpointInfo the {@code EndpointInfo} object containing the configuration details for the service
     * @throws ServiceRegistrationException if the service cannot be added
     */
    public void addService(EndpointInfo endpointInfo) throws ServiceRegistrationException
    {
        String serviceName = endpointInfo.getServiceName();

        try
        {
            endpointRegistry.addEndpoint(endpointInfo);
        }

        catch (ServiceRegistrationException ex)
        {
            logger.error("Could not start service: " + ex.getMessage());
            throw new ServiceRegistrationException("Failed to start service: " + serviceName, ex.getCause());
        }

        try
        {
            wsEndpointManager.deployEndpoint(endpointInfo);
        }

        catch (Exception ex)
        {
            logger.error("Could not deploy endpoint: " + ex.getMessage());
            endpointRegistry.removeEndpoint(serviceName);
            throw new ServiceRegistrationException("Failed to deploy endpoint: " + serviceName, ex.getCause());
        }

        if (endpointInfo.isPublic())
        {
            System.out.println("IT WAS PUBLIC");
            try
            {
                serviceBroadcastManager.broadcastService(endpointInfo);
            }

            catch (Exception ex)
            {
                logger.error("Could not broadcast service: " + ex.getMessage());
                endpointRegistry.removeEndpoint(serviceName);
                try
                {
                    wsEndpointManager.shutdown(serviceName);
                }

                catch (WsEndpointUndeploymentException e)
                {
                    logger.error("Could not undeploy endpoint: " + e.getMessage());
                }

                throw new ServiceRegistrationException("Failed to broadcast service: " + serviceName, ex.getCause());
            }
        }
    }


    /**
     * Removes the service with the specified service name.
     *
     * @param serviceName the name of the service to be removed
     * @throws WsEndpointUndeploymentException if the service cannot be undeployed
     */
    public void removeService(String serviceName) throws WsEndpointUndeploymentException
    {
        try
        {
            EndpointInfo endpointInfo = endpointRegistry.getEndpointInfo(serviceName);

            if (endpointInfo == null)
            {
                logger.warn("Endpoint not found: " + serviceName);
                return;
            }

            if (endpointInfo.isPublic())
            {
                try
                {
                    serviceBroadcastManager.stopBroadcastingService(serviceName);
                }
                catch (Exception ex)
                {
                    logger.error("Could not un-broadcast service: " + ex.getMessage());
                    throw new WsEndpointUndeploymentException("Failed to un-broadcast service: " + serviceName, ex.getCause());
                }
            }

            try
            {
                wsEndpointManager.shutdown(serviceName);
            }
            catch (Exception ex)
            {
                logger.error("Could not undeploy endpoint: " + ex.getMessage());
                throw new WsEndpointUndeploymentException("Failed to undeploy endpoint: " + serviceName, ex.getCause());
            }

            endpointRegistry.removeEndpoint(serviceName);
        }
        catch (Exception ex)
        {
            logger.error("Could not remove endpoint: " + ex.getMessage());
            throw new WsEndpointUndeploymentException("Failed to remove endpoint: " + serviceName, ex.getCause());
        }
    }


    /**
     * Removes all registered services.
     */
    public void removeAll()
    {
        List<EndpointInfo> endpointInfos = endpointRegistry.getEndpoints();

        for(EndpointInfo endpointInfo : endpointInfos)
        {
            if(endpointInfo.isPublic())
            {
                serviceBroadcastManager.stopBroadcastingService(endpointInfo.getServiceName());
            }

            try
            {
                wsEndpointManager.shutdown(endpointInfo.getServiceName());
            }

            catch (WsEndpointUndeploymentException ex)
            {
                logger.error("Failed to kill server.");
            }
        }
    }


    /**
     * Gets a list of all registered services.
     *
     * @return a list of {@code EndpointInfo} objects representing the registered services
     */
    public List<EndpointInfo> getAllServices()
    {
        return endpointRegistry.getEndpoints();
    }


    /**
     * Retrieves the number of connections for the specified service.
     *
     * @param serviceName the name of the service
     * @return the number of connections for the specified service
     */
    public int getConnections(String serviceName)
    {
        return wsEndpointManager.getConnections(serviceName);
    }


    /**
     * Retrieves the total number of connections for all services.
     *
     * @return the total number of connections for all services
     */
    public int getTotalConnections()
    {
        return wsEndpointManager.getTotalConnections();
    }
}
