package MMS.EdgeRouter.ServiceRegistry;

import java.util.HashMap;

public class ServiceRegistry
{
    private HashMap<String, EdgeRouterService> services;

    private static ServiceRegistry instance;

    private ServiceRegistry()
    {
        services = new HashMap<>();
    }

    public static ServiceRegistry getRegistry()
    {
        if (instance == null)
        {
            instance = new ServiceRegistry();
        }
        return instance;
    }

    public void addService(EdgeRouterService service)
    {
        services.put(service.getServiceName(), service);
    }

    public EdgeRouterService getService(String serviceName)
    {
        return services.get(serviceName);
    }

    public void removeService(String serviceName)
    {
        services.remove(serviceName);
    }

    public HashMap<String, EdgeRouterService> getServices()
    {
        return new HashMap<>(services);
    }
}
