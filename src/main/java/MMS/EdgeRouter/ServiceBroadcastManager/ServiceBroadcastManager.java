package MMS.EdgeRouter.ServiceBroadcastManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;

public class ServiceBroadcastManager
{
    private static final Logger logger = LogManager.getLogger(ServiceBroadcastManager.class);
    private static final String SERVICE_TYPE = "_mms-edge-router._tcp.local.";

    private final ServiceRegistry serviceRegistry;
    private final BroadcastService broadcastService;


    public ServiceBroadcastManager()
    {
        try
        {
            this.serviceRegistry = new ServiceRegistry();
            this.broadcastService = new BroadcastService();
            logger.info("ServiceBroadcastManager created");
        }

        catch (IOException ex)
        {
            logger.fatal("A fatal error occurred while creating ServiceBroadcastManager", ex);
            throw new RuntimeException(ex);
        }
    }


    public void broadcastService(String serviceName, int servicePort, String path) throws IOException
    {
        ServiceInfo serviceInfo = ServiceInfo.create(SERVICE_TYPE, serviceName, servicePort, "path=" + path);
        broadcastService.broadcastService(serviceInfo);
        serviceRegistry.registerService(serviceInfo);
        logger.info("ServiceBroadcastManager broadcast service " + serviceInfo.getName());
    }


    public void stopBroadcastingService(String serviceName) throws IllegalArgumentException
    {
        ServiceInfo serviceInfo = serviceRegistry.getServiceInfo(serviceName);

        if (serviceInfo == null)
            throw new IllegalArgumentException("Service " + serviceName + " is not registered");

        broadcastService.stopBroadcastingService(serviceInfo);
        serviceRegistry.unregisterService(serviceName);
        logger.info("ServiceBroadcastManager stopped broadcasting service " + serviceName);
    }


    public void stopBroadcastingAllServices()
    {
        broadcastService.stopBroadcastingAllServices();
        serviceRegistry.clear();
        logger.info("ServiceBroadcastManager stopped broadcasting all services");
    }
}
