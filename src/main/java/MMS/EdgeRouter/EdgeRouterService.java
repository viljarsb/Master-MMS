package MMS.EdgeRouter;

import org.eclipse.jetty.servlets.DoSFilter;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class EdgeRouterService
{
    private String serviceName;
    private String servicePath;
    private int servicePort;
    private Boolean isPublic;
    private String localAddress;

    private int maxConnections;

    private int maxRequestsPerSec;
    private long delayMs;
    private long throttleMs;
    private boolean trackSessions;
    private Set<String> whitelist;
    private int tooManyCode;


    public EdgeRouterService(String serviceName, String servicePath, int servicePort, Boolean isPublic)
    {
        this.serviceName = serviceName;
        this.servicePath = servicePath;
        this.servicePort = servicePort;
        this.isPublic = isPublic;

        this.maxConnections = 255;
        this.maxRequestsPerSec = 100;
        this.delayMs = 100;
        this.throttleMs = 1000;
        this.trackSessions = true;
        this.whitelist = new HashSet<>();
        this.tooManyCode = 429;
        this.localAddress = getLocalIPAddress();
    }

    public String getLocalAddress()
    {
        return localAddress;
    }

    private String getLocalIPAddress()
    {
        try
        {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements())
            {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements())
                {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress.getHostAddress().indexOf(':') == -1)
                    {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        }
        catch (SocketException e)
        {
            e.printStackTrace();
        }
        return null;
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


    public Boolean isPublic()
    {
        return isPublic;
    }


    public int getMaxConnections()
    {
        return maxConnections;
    }


    public int getMaxRequestsPerSec()
    {
        return maxRequestsPerSec;
    }


    public long getDelayMs()
    {
        return delayMs;
    }


    public long getThrottleMs()
    {
        return throttleMs;
    }


    public boolean isTrackSessions()
    {
        return trackSessions;
    }


    public String getWhitelist()
    {
        return String.join(",", whitelist);
    }


    public int getTooManyCode()
    {
        return tooManyCode;
    }


}
