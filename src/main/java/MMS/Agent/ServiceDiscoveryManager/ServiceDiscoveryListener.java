package MMS.Agent.ServiceDiscoveryManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.util.ArrayList;

public class ServiceDiscoveryListener
{
    private static final Logger logger = LogManager.getLogger(ServiceDiscoveryListener.class);
    private static final String SERVICE_TYPE = "_mms-edge-router._tcp.local.";
    private static final long SERVICE_TIMEOUT = 6000;

    private final JmDNS jmDNS;


    public ServiceDiscoveryListener()
    {
        try
        {
            jmDNS = JmDNS.create();
            logger.info("ServiceDiscoveryListener created");
        }

        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public synchronized ArrayList<RouterInfo> listen()
    {
        logger.info("ServiceDiscoveryListener started");
        ServiceInfo[] serviceInfos = jmDNS.list(SERVICE_TYPE, SERVICE_TIMEOUT);
        logger.info("ServiceDiscoveryListener stopped, found " + serviceInfos.length + " services");
        return assembleRouterInfo(serviceInfos);
    }

    private ArrayList<RouterInfo> assembleRouterInfo(ServiceInfo[] serviceInfos)
    {
        ArrayList<RouterInfo> routerInfos = new ArrayList<>();
        for (ServiceInfo serviceInfo : serviceInfos)
        {
            RouterInfo routerService = new RouterInfo(serviceInfo);
            routerInfos.add(routerService);
        }

        return routerInfos;
    }
}
