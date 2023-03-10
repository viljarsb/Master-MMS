package MMS.EdgeRouter.ServiceBroadcastManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;

public class ServiceBroadcastManager
{
    private static final Logger logger = LogManager.getLogger(ServiceBroadcastManager.class);
    private final ServiceRegistry serviceRegistry;
    private static final String SERVICE_TYPE = "_mms-edge-router._tcp.local.";
    private final JmDNS jmdns;


    public ServiceBroadcastManager()
    {
        try
        {
            this.serviceRegistry = new ServiceRegistry();
            this.jmdns = JmDNS.create();
            logger.info("ServiceBroadcastManager created");
        }

        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }


    public void broadcastService(String serviceName, int servicePort, String path) throws IOException
    {
        ServiceInfo serviceInfo = ServiceInfo.create(SERVICE_TYPE, serviceName, servicePort, "path=" + path);
        serviceRegistry.registerService(serviceInfo);
        this.jmdns.registerService(serviceInfo);
        logger.info("ServiceBroadcastManager broadcast service " + serviceInfo.getName());
    }


    public void stopBroadcastingService(String serviceName)
    {
        ServiceInfo serviceInfo = this.serviceRegistry.getServiceInfo(serviceName);
        this.jmdns.unregisterService(serviceInfo);
        this.serviceRegistry.unregisterService(serviceName);
        logger.info("ServiceBroadcastManager stopped broadcasting service " + serviceName);
    }


    public void stopBroadcastingAllServices()
    {
        this.jmdns.unregisterAllServices();
        this.serviceRegistry.clear();
        logger.info("ServiceBroadcastManager stopped broadcasting all services");
    }


    public void close()
    {
        try
        {
            this.jmdns.close();
            this.serviceRegistry.clear();
            logger.info("ServiceBroadcastManager closed quietly");
        }

        catch (Exception ex)
        {
            logger.error("ServiceBroadcastManager failed to close quietly");
        }
    }
}
