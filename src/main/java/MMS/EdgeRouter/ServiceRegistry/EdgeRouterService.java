package MMS.EdgeRouter.ServiceRegistry;

public class EdgeRouterService
{
    private String serviceName;
    private String servicePath;
    private int servicePort;
    private boolean isPublic;

    public EdgeRouterService(String serviceName, String servicePath, int servicePort, boolean isPublic)
    {
        this.serviceName = serviceName;
        this.servicePath = servicePath;
        this.servicePort = servicePort;
        this.isPublic = isPublic;
    }

    public String getServiceName()
    {
        return serviceName;
    }

    public String getServicePath()
    {
        return servicePath;
    }

    public int getServicePort()
    {
        return servicePort;
    }

    public boolean isPublic()
    {
        return isPublic;
    }
}
