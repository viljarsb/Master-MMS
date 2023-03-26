package MMS.EdgeRouter.ServiceRegistry;

import MMS.EdgeRouter.Exceptions.ServiceRegistrationException;

import java.util.*;

/**
 * This class serves as a registry for managing {@link EndpointInfo} objects.
 * It provides methods to add, remove, and retrieve endpoint information based
 * on the service name. Additionally, it allows for retrieval of all registered
 * endpoints.
 */
public class EndpointRegistry
{
    private final Map<String, EndpointInfo> endpointRegistry;


    /**
     * Constructs a new endpoint registry instance.
     */
    public EndpointRegistry()
    {
        endpointRegistry = new HashMap<>();
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

        if (endpointRegistry.containsKey(serviceName))
        {
            throw new ServiceRegistrationException("A service with the name " + serviceName + " is already registered");
        }

        for (EndpointInfo existingEndpoint : endpointRegistry.values())
        {
            if (existingEndpoint.equals(endpointInfo))
            {
                throw new ServiceRegistrationException("Service clash: " + existingEndpoint + " and " + endpointInfo);
            }
        }

        endpointRegistry.put(serviceName, endpointInfo);
    }


    /**
     * Removes an endpoint from the registry by its service name.
     *
     * @param serviceName The name of the service to be removed.
     */
    public void removeEndpoint(String serviceName)
    {
        endpointRegistry.remove(serviceName);
    }


    /**
     * Retrieves the {@link EndpointInfo} object associated with the given service name.
     *
     * @param serviceName The name of the service.
     * @return The {@link EndpointInfo} object associated with the service, or null if not found.
     */
    public EndpointInfo getEndpointInfo(String serviceName)
    {
        return endpointRegistry.get(serviceName);
    }


    /**
     * Retrieves a list of all {@link EndpointInfo} objects currently registered.
     *
     * @return A {@link List} of {@link EndpointInfo} objects representing all registered endpoints.
     */
    public List<EndpointInfo> getEndpoints()
    {
        return new ArrayList<>(endpointRegistry.values());
    }
}
