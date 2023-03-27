package MMS.EdgeRouter;

import org.eclipse.jetty.server.*;

import javax.jmdns.ServiceInfo;


public final class EdgeRouterService
{
    private final Server server;
    private final ServiceInfo serviceInfo;


    public EdgeRouterService(Server server, ServiceInfo serviceInfo)
    {
        this.server = server;
        this.serviceInfo = serviceInfo;
    }


    public Server getServer()
    {
        return server;
    }


    public ServiceInfo getServiceInfo()
    {
        return serviceInfo;
    }
}
