package MMS.EdgeRouter.ServiceBroadcastManager;

import javax.jmdns.ServiceInfo;
import java.util.HashMap;
import java.util.Map;

public class BroadcastRegistry
{
    private final HashMap<String, ServiceInfo> serviceInfoMap;


    BroadcastRegistry()
    {
        this.serviceInfoMap = new HashMap<>();
    }


    void registerService(ServiceInfo serviceInfo)
    {
        this.serviceInfoMap.put(serviceInfo.getName(), serviceInfo);
    }


    void unregisterService(String serviceName)
    {
        this.serviceInfoMap.remove(serviceName);
    }


    ServiceInfo getServiceInfo(String serviceName)
    {
        return this.serviceInfoMap.get(serviceName);
    }


    Map<String, ServiceInfo> getServiceInfoMap()
    {
        return new HashMap<>(this.serviceInfoMap);
    }


    void clear()
    {
        this.serviceInfoMap.clear();
    }
}
