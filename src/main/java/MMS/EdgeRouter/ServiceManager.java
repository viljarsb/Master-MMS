package MMS.EdgeRouter;

import MMS.EdgeRouter.Exceptions.*;
import MMS.EdgeRouter.ServiceBroadcast.ServiceBroadcastManager;
import MMS.EdgeRouter.ServiceRegistry.EndpointInfo;
import MMS.EdgeRouter.ServiceRegistry.ServiceRegistry;
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

    private final ServiceRegistry serviceRegistry;
    private final ServiceBroadcastManager serviceBroadcastManager;
    private final WsEndpointManager wsEndpointManager;


    /**
     * Constructs a new {@code ServiceManager} instance.
     */
    private ServiceManager()
    {
        this.serviceRegistry = ServiceRegistry.getRegistry();
        this.serviceBroadcastManager = ServiceBroadcastManager.getManager();
        this.wsEndpointManager = WsEndpointManager.getManager();
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
            serviceRegistry.addEndpoint(endpointInfo);
        }

        catch (ServiceRegistrationException ex)
        {
            logger.error("There was a problem while registering the edge router service: '{}': {}", serviceName, ex.getMessage(), ex);
            throw new ServiceRegistrationException("Failed to register service: " + serviceName, ex.getCause());
        }

        try
        {
            wsEndpointManager.deployEndpoint(endpointInfo);
        }

        catch (WsEndpointDeploymentException ex)
        {
            logger.error("There was a problem while deploying the edge router service '{}': {}", serviceName, ex.getMessage(), ex);
            serviceRegistry.removeEndpoint(serviceName);
            throw new ServiceRegistrationException("Failed to deploy endpoint: " + serviceName, ex.getCause());
        }

        if (endpointInfo.isPublic())
        {
            try
            {
                serviceBroadcastManager.broadcastService(endpointInfo);
            }

            catch (ServiceBroadcastException ex)
            {
                logger.error("There was a problem while registering the service '{}' for broadcast: {}", serviceName, ex.getMessage(), ex);
                serviceRegistry.removeEndpoint(serviceName);

                try
                {
                    wsEndpointManager.shutdown(serviceName);
                }

                catch (WsEndpointUndeploymentException e)
                {
                    logger.error("Failed to undeploy service: '{}' after failed broadcast: {}", serviceName, e.getMessage(), ex);
                }

                throw new ServiceRegistrationException("Failed to broadcast service: " + serviceName, ex.getCause());
            }
        }
    }


    /**
     * Removes the service with the specified service name.
     *
     * @param serviceName the name of the service to be removed
     * @throws ServiceUnregistrationException if an error occurs while removing the service
     */
    public void removeService(String serviceName) throws ServiceUnregistrationException
    {
        try
        {
            EndpointInfo endpointInfo = serviceRegistry.getEndpointInfo(serviceName);

            if (endpointInfo == null)
            {
                logger.warn("Endpoint not found: " + serviceName);
                throw new NoSuchServiceException("No service with the name " + serviceName + " is registered.");
            }

            if (endpointInfo.isPublic())
            {
                serviceBroadcastManager.stopBroadcastingService(serviceName);
                logger.info("Service '{}' un-broadcasted.", serviceName);
            }

            wsEndpointManager.shutdown(serviceName);
            serviceRegistry.removeEndpoint(serviceName);
        }

        catch (NoSuchServiceException ex)
        {
            logger.error("The service '{}' is not registered.", serviceName);
            throw new ServiceUnregistrationException("No service with the name " + serviceName + " is registered.", ex.getCause());
        }

        catch (WsEndpointUndeploymentException e)
        {
            logger.error("Failed to undeploy ws endpoint for service '{}': {}", serviceName, e.getMessage(), e);
            throw new ServiceUnregistrationException("An error occurred when attempting to undeploy the Ws endpoint: " + serviceName, e.getCause());
        }
    }


    /**
     * Removes all registered services.
     */
    public void removeAll()
    {
        List<EndpointInfo> endpointInfos = serviceRegistry.getEndpoints();

        for (EndpointInfo endpointInfo : endpointInfos)
        {
            if (endpointInfo.isPublic())
            {
                serviceBroadcastManager.stopBroadcastingService(endpointInfo.getServiceName());
            }

            try
            {
                wsEndpointManager.shutdown(endpointInfo.getServiceName());
            }

            catch (WsEndpointUndeploymentException ex)
            {
                logger.info("Failed to undeploy service: '{}' during shutdown: {}", endpointInfo.getServiceName(), ex.getMessage(), ex);
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
        return serviceRegistry.getEndpoints();
    }


    /**
     * Retrieves the number of connections for the specified service.
     *
     * @param serviceName the name of the service
     * @return the number of connections for the specified service
     * @throws NoSuchServiceException if the service does not exist
     */
    public int getConnections(String serviceName) throws NoSuchServiceException
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
