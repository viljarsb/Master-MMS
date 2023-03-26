package MMS.EdgeRouter.ServiceBroadcast;

import MMS.EdgeRouter.Exceptions.ServiceBroadcastException;
import MMS.EdgeRouter.ServiceRegistry.EndpointInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * {@code ServiceBroadcastManager} is a class responsible for broadcasting service information and managing the broadcasted services.
 * It maintains a collection of {@link ServiceBroadcaster} instances for different interfaces and a collection of {@link ServiceInfo} instances.
 * <p>
 * The {@link ServiceInfo} instances represent broadcasted edge router services, and broadcast DNS resource records with the necessary
 * information needed by MMS Agents to connect.
 */
public class ServiceBroadcastManager
{
    private static final Logger logger = LogManager.getLogger(ServiceBroadcastManager.class);
    private static final String SERVICE_TYPE = "_mms-edge-router._tcp.local.";

    private final Map<String, ServiceBroadcaster> serviceBroadcasters;
    private final Map<String, ServiceInfo> services;


    /**
     * Constructor for the ServiceBroadcastManager class.
     * Initializes the serviceBroadcasters and services maps.
     */
    public ServiceBroadcastManager()
    {
        this.serviceBroadcasters = new HashMap<>();
        this.services = new HashMap<>();
    }


    /**
     * Broadcasts the service information provided by the {@link EndpointInfo} object.
     *
     * @param endpointInfo An {@link EndpointInfo} object containing service information such as name, port, path, and interface address.
     * @throws ServiceBroadcastException If the service is already broadcasted or a {@link ServiceBroadcaster} could not be created for the specified interface address.
     */
    public void broadcastService(EndpointInfo endpointInfo) throws ServiceBroadcastException
    {
        String serviceName = endpointInfo.getServiceName();
        int servicePort = endpointInfo.getServicePort();
        String servicePath = endpointInfo.getServicePath();
        String interfaceAddress = endpointInfo.getAddress();

        ServiceInfo serviceInfo = ServiceInfo.create(SERVICE_TYPE, serviceName, servicePort, "Path=" + servicePath);

        String additionalData = endpointInfo.getAdditionalData();

        if(additionalData != null)
            serviceInfo.setText(additionalData.getBytes());

        for (ServiceInfo service : services.values())
        {
            if (service.equals(serviceInfo))
            {
                logger.info("Service already broadcasted");
                throw new ServiceBroadcastException("Service with name " + serviceName + " already broadcasted");
            }
        }

        if (!serviceBroadcasters.containsKey(interfaceAddress))
        {
            try
            {
                ServiceBroadcaster broadcaster = new ServiceBroadcaster(interfaceAddress);
                serviceBroadcasters.put(interfaceAddress, broadcaster);
                broadcaster.broadcastService(serviceInfo);
                services.put(serviceName, serviceInfo);
            }

            catch (IOException ex)
            {
                logger.error("Could not create service broadcaster for interface " + interfaceAddress);
                throw new ServiceBroadcastException("Could not create service broadcaster for interface " + interfaceAddress, ex.getCause());
            }
        }
    }


    /**
     * Stops broadcasting the service with the specified service name.
     *
     * @param serviceName The name of the service to stop broadcasting.
     */
    public void stopBroadcastingService(String serviceName)
    {
        ServiceInfo serviceInfo = services.get(serviceName);
        String interfaceAddress = serviceInfo.getInetAddresses()[0].getHostAddress();
        ServiceBroadcaster broadcaster = serviceBroadcasters.get(interfaceAddress);
        broadcaster.stopBroadcastingService(serviceInfo);
        services.remove(serviceName);

        if(broadcaster.getBroadcastCounter() < 1)
        {
            try
            {
                broadcaster.close();
                serviceBroadcasters.remove(interfaceAddress);
            }

            catch (IOException ignored) {}
        }
    }



    /**
     * Stops broadcasting all services and closes all {@link ServiceBroadcaster} instances.
     */
    public void stopAllBroadcasts()
    {
        for (ServiceBroadcaster broadcaster : serviceBroadcasters.values())
        {
            broadcaster.stopBroadcastingAllServices();

            try
            {
                broadcaster.close();
            }

            catch (IOException ignored) {}
        }

        serviceBroadcasters.clear();
    }
}
