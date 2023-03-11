package MMS.EdgeRouter.ServiceBroadcastManager;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;

public class BroadcastService
{
    private final JmDNS jmdns;


    BroadcastService() throws IOException
    {
        jmdns = JmDNS.create();
    }


    void broadcastService(ServiceInfo serviceInfo) throws IOException
    {
        jmdns.registerService(serviceInfo);
    }


    void stopBroadcastingService(ServiceInfo serviceInfo)
    {
        jmdns.unregisterService(serviceInfo);
    }


    void stopBroadcastingAllServices()
    {
        jmdns.unregisterAllServices();
    }
}
