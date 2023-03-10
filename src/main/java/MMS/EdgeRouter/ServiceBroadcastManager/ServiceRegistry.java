package MMS.EdgeRouter.ServiceBroadcastManager;

import javax.jmdns.ServiceInfo;
import java.util.HashMap;
import java.util.Map;

public class ServiceRegistry
{
    private HashMap<String, ServiceInfo> serviceInfoMap;


    public ServiceRegistry()
    {
        this.serviceInfoMap = new HashMap<String, ServiceInfo>();
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
        return new HashMap<String, ServiceInfo>(this.serviceInfoMap);
    }


    void clear()
    {
        this.serviceInfoMap.clear();
    }
}
