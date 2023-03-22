package MMS.Agent.ServiceDiscoveryListner;

import javax.jmdns.ServiceInfo;


/**
 * The {@code RouterInfo} class provides a convenient way to store information about
 * a discovered router service and extract the information needed to connect to the
 * router service.
 */
public class RouterInfo
{
    private final String serviceName;
    private final String serviceIP;
    private final String servicePort;
    private final String servicePath;


    public RouterInfo(ServiceInfo serviceInfo)
    {
        this.serviceName = serviceInfo.getName();
        this.serviceIP = serviceInfo.getHostAddresses()[0];
        this.servicePort = String.valueOf(serviceInfo.getPort());
        this.servicePath = serviceInfo.getPropertyString("path");
    }


    public String getServiceName()
    {
        return serviceName;
    }


    public String getServiceIP()
    {
        return serviceIP;
    }


    public String getServicePort()
    {
        return servicePort;
    }


    public String getServicePath()
    {
        return servicePath;
    }


    public String getUri()
    {
        return "wss://" + this.serviceIP + ":" + this.servicePort + this.servicePath;
    }
}
