package MMS.EdgeRouter.ServiceBroadcastManager;

import MMS.EdgeRouter.EdgeRouterService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.util.HashMap;

public class ServiceBroadcastManager
{
    private final static Logger logger = LogManager.getLogger(ServiceBroadcastManager.class);
    private static final String SERVICE_TYPE = "_mms-edge-router._tcp.local.";
    private static ServiceBroadcastManager instance;
    private ServiceInfo serviceInfo;
    private final ServiceBroadcastService serviceBroadcastService;

    private ServiceBroadcastManager() throws IOException
    {
        serviceBroadcastService = new ServiceBroadcastService();
    }


    public static ServiceBroadcastManager getManager()
    {
        if (instance == null)
        {
            try
            {
                instance = new ServiceBroadcastManager();
            }

            catch (IOException ex)
            {
                logger.fatal("A fatal error occurred while initializing the Service Broadcast Manager.", ex);
                System.exit(1);
            }
        }
        return instance;
    }


    public void broadcastService(EdgeRouterService service) throws IOException
    {
        ServiceInfo serviceInfo = ServiceInfo.create(SERVICE_TYPE, service.getServiceName(), service.getServicePort(), "path=" + service.getServicePath());
        serviceBroadcastService.broadcast(serviceInfo);
        this.serviceInfo = serviceInfo;
    }


    public void stopBroadcastingService()
    {
        if (serviceInfo != null)
        {
            serviceBroadcastService.stopBroadcasting(serviceInfo);
        }
    }
}
