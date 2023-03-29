package MMS.EdgeRouter;

import MMS.EdgeRouter.Exceptions.ServiceBroadcastException;
import MMS.EdgeRouter.Exceptions.ServiceBroadcasterCreationException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;

public class ServiceBroadcaster
{
    private static final String SERVICE_TYPE = "_mms-edge-router._tcp.local.";
    private final JmDNS jmdns;
    private final ServiceInfo broadcastInfo;
    private boolean isRunning = false;

    private ServiceBroadcaster(InetAddress address, ServiceInfo broadcastInfo) throws IOException
    {
        this.jmdns = JmDNS.create(address);
        this.broadcastInfo = broadcastInfo;
    }

    public static ServiceBroadcaster create(InetAddress address, String serviceName, int servicePort, String servicePath, String serviceDescription) throws ServiceBroadcasterCreationException
    {
        try
        {
            ServiceInfo broadcastInfo = ServiceInfo.create(SERVICE_TYPE, serviceName, servicePort, servicePath);

            if(serviceDescription != null)
                broadcastInfo.setText(serviceDescription.getBytes());

            return new ServiceBroadcaster(address, broadcastInfo);
        }

        catch (IOException ex)
        {
            throw new ServiceBroadcasterCreationException("Error creating service broadcaster: " + ex.getMessage(), ex.getCause());
        }
    }


    public void start() throws ServiceBroadcastException
    {
        if (isRunning)
        {
            throw new ServiceBroadcastException("Service broadcaster already running.");
        }

        try
        {
            jmdns.registerService(broadcastInfo);
            isRunning = true;
        }

        catch (IOException ex)
        {
            throw new ServiceBroadcastException("Error broadcasting service.", ex);
        }
    }



    public void shutdown()
    {
        jmdns.unregisterAllServices();

        try
        {
            jmdns.close();
        }

        catch (IOException ignored) {}
    }
}
