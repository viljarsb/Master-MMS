package MMS.EdgeRouter.ServiceRegistry;

import MMS.EdgeRouter.Exceptions.ServiceRegistrationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;


/**
 * This class serves as a registry for managing {@link EndpointInfo} objects.
 * It provides methods to add, remove, and retrieve endpoint information based
 * on the service name. Additionally, it allows for retrieval of all registered
 * endpoints.
 */
public class ServiceRegistry
{
    private final static Logger logger = LogManager.getLogger(ServiceRegistry.class);
    private static ServiceRegistry instance;

    private final Map<String, EndpointInfo> serviceRegistry = new HashMap<>();


    /**
     * Constructs a new endpoint registry instance.
     */
    private ServiceRegistry()
    {
        logger.info("Endpoint registry initialized");
    }


    /**
     * Returns the singleton instance of the endpoint registry.
     *
     * @return The singleton instance of the endpoint registry.
     */
    public synchronized static ServiceRegistry getRegistry()
    {
        if (instance == null)
            instance = new ServiceRegistry();

        return instance;
    }


    /**
     * Adds an {@link EndpointInfo} object to the registry.
     *
     * @param endpointInfo The {@link EndpointInfo} object to be added.
     * @throws ServiceRegistrationException If a service with the same name or a clashing endpoint already exists.
     */
    public void addEndpoint(EndpointInfo endpointInfo) throws ServiceRegistrationException
    {
        String serviceName = endpointInfo.getServiceName();

        for (EndpointInfo registeredService : serviceRegistry.values())
        {
            if (registeredService.equals(endpointInfo))
            {
                logger.info("Failed to register service: {} - Service already exists", endpointInfo);
                throw new ServiceRegistrationException("Service already exists");
            }
        }

        logger.info("Registered service: {}", endpointInfo);
        serviceRegistry.put(serviceName, endpointInfo);
    }


    /**
     * Removes an endpoint from the registry by its service name.
     *
     * @param serviceName The name of the service to be removed.
     * @return The {@link EndpointInfo} object that was removed, or null if not found.
     */
    public EndpointInfo removeEndpoint(String serviceName)
    {
        return serviceRegistry.remove(serviceName);
    }


    /**
     * Retrieves the {@link EndpointInfo} object associated with the given service name.
     *
     * @param serviceName The name of the service.
     * @return The {@link EndpointInfo} object associated with the service, or null if not found.
     */
    public EndpointInfo getEndpointInfo(String serviceName)
    {
        return serviceRegistry.get(serviceName);
    }


    /**
     * Retrieves a list of all {@link EndpointInfo} objects currently registered.
     *
     * @return A {@link List} of {@link EndpointInfo} objects representing all registered endpoints.
     */
    public List<EndpointInfo> getEndpoints()
    {
        return new ArrayList<>(serviceRegistry.values());
    }
}
