package MMS.EdgeRouter.ServiceRegistry;

import MMS.EdgeRouter.ServiceBroadcastManager.ServiceBroadcastManager;
import MMS.EdgeRouter.WebSocketManager.WebSocketManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;

public class ServiceRegistry
{
    private final HashMap<String, EdgeRouterService> services;
    private static ServiceRegistry instance;


    private ServiceRegistry()
    {
        this.services = new HashMap<>();
    }


    public static ServiceRegistry getRegistry()
    {
        if (instance == null)
        {
            instance = new ServiceRegistry();
        }

        return instance;
    }


    public void addService(EdgeRouterService service) throws IllegalStateException
    {
        if (services.containsKey(service.getServiceName()))
        {
            throw new IllegalStateException("Service already exists.\n" + services.get(service.getServiceName()).toString() + "\n");
        }

        for (EdgeRouterService s : services.values())
        {
            if (s.getServicePath().equals(service.getServicePath()) && s.getServicePort() == service.getServicePort())
            {
                throw new IllegalStateException("A service with the same path and port already exists.\n" + s + "\n");
            }
        }

        services.put(service.getServiceName(), service);
    }


    public void removeService(String serviceName) throws NoSuchElementException
    {
        if (!services.containsKey(serviceName))
        {
            throw new NoSuchElementException("Service does not exist.\n");
        }

        services.remove(serviceName);
    }


    public EdgeRouterService getService(String serviceName) throws NoSuchElementException
    {
        if (!services.containsKey(serviceName))
        {
            throw new NoSuchElementException("Service does not exist.\n");
        }

        return services.get(serviceName);
    }


    public HashMap<String, EdgeRouterService> getAllServices()
    {
        return new HashMap<>(services);
    }
}
