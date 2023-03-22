package MMS.EdgeRouter.ServiceBroadcastManager;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.util.HashMap;

public class ServiceBroadcastService
{
    private final JmDNS jmdns;


    ServiceBroadcastService() throws IOException
    {
        this.jmdns = JmDNS.create();
    }


    void broadcast(ServiceInfo serviceInfo) throws IOException
    {
        jmdns.registerService(serviceInfo);
    }


    void stopBroadcasting(ServiceInfo serviceInfo)
    {
        jmdns.unregisterService(serviceInfo);
    }


    void stopBroadcastingAll()
    {
        jmdns.unregisterAllServices();
    }

}
