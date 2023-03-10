package MMS.Agent.ServiceDiscoveryManager;

import javax.jmdns.ServiceInfo;

public class RouterInfo
{
    private String serviceName;
    private String serviceIP;
    private String servicePort;
    private String servicePath;

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
