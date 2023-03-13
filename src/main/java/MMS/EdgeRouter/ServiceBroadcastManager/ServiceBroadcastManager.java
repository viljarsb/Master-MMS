package MMS.EdgeRouter.ServiceBroadcastManager;

import MMS.EdgeRouter.ServiceRegistry.EdgeRouterService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jmdns.ServiceInfo;
import java.io.IOException;

public class ServiceBroadcastManager
{
    private static final Logger logger = LogManager.getLogger(ServiceBroadcastManager.class);
    private static final String SERVICE_TYPE = "_mms-edge-router._tcp.local.";

    private final BroadcastRegistry broadcastRegistry;
    private final BroadcastService broadcastService;
    private static ServiceBroadcastManager instance;


    private ServiceBroadcastManager()
    {
        try
        {
            this.broadcastRegistry = new BroadcastRegistry();
            this.broadcastService = new BroadcastService();
            logger.info("ServiceBroadcastManager created");
        }

        catch (IOException ex)
        {
            logger.fatal("A fatal error occurred while creating ServiceBroadcastManager", ex);
            throw new RuntimeException(ex);
        }
    }


    public static ServiceBroadcastManager getManager()
    {
        if (instance == null)
        {
            instance = new ServiceBroadcastManager();
        }

        return instance;
    }


    public void broadcastService(EdgeRouterService service) throws IOException
    {
        ServiceInfo serviceInfo = ServiceInfo.create(SERVICE_TYPE, service.getServiceName(), service.getServicePort(), "path=" + service.getServicePath());
        broadcastService.broadcastService(serviceInfo);
        broadcastRegistry.registerService(serviceInfo);
        logger.info("ServiceBroadcastManager broadcast service " + serviceInfo.getName());
    }


    public void stopBroadcastingService(EdgeRouterService service) throws IllegalArgumentException
    {
        ServiceInfo serviceInfo = broadcastRegistry.getServiceInfo(service.getServiceName());

        if (serviceInfo == null)
            throw new IllegalArgumentException("Service " + service.getServiceName() + " is not registered");

        broadcastService.stopBroadcastingService(serviceInfo);
        broadcastRegistry.unregisterService(service.getServiceName());
        logger.info("ServiceBroadcastManager stopped broadcasting service " + service.getServiceName());
    }


    public void stopBroadcastingAllServices()
    {
        broadcastService.stopBroadcastingAllServices();
        broadcastRegistry.clear();
        logger.info("ServiceBroadcastManager stopped broadcasting all services");
    }
}
