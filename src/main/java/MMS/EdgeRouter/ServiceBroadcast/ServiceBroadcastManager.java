package MMS.EdgeRouter.ServiceBroadcast;

import MMS.EdgeRouter.Exceptions.ServiceBroadcastException;
import MMS.EdgeRouter.ServiceRegistry.EndpointInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jmdns.ServiceInfo;
import java.io.IOException;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is responsible for managing the broadcasting of MMS edge router services using mDNS protocol.
 * It uses the {@code ServiceBroadcaster} class to actually broadcast the services.
 * <p>
 * In addition, it keeps track of all the services that have been broadcasted. It also provides methods to stop
 * broadcasting a service and to get the list of all currently broadcasted services.
 */
public final class ServiceBroadcastManager
{
    private static final Logger logger = LogManager.getLogger(ServiceBroadcastManager.class);
    private static ServiceBroadcastManager instance;
    private static final String SERVICE_TYPE = "_mms-edge-router._tcp.local.";

    private final Map<InetAddress, ServiceBroadcaster> serviceBroadcasters = new ConcurrentHashMap<>();
    private final Map<String, ServiceInfo> services = new ConcurrentHashMap<>();


    /**
     * Constructor for the ServiceBroadcastManager class.
     * Initializes the serviceBroadcasters and services maps.
     */
    private ServiceBroadcastManager()
    {
        logger.info("Service Broadcast Manager Initialized");
    }


    /**
     * Gets the singleton instance of ServiceBroadcastManager.
     *
     * @return The singleton instance of ServiceBroadcastManager.
     */
    public synchronized static ServiceBroadcastManager getManager()
    {
        if (instance == null)
            instance = new ServiceBroadcastManager();

        return instance;
    }


    /**
     * Broadcasts the given service using the ServiceBroadcaster class, what network interface
     * to use is determined by the address specified in the endpointInfo parameter.
     *
     * @param endpointInfo The information about the endpoint that provides the service.
     * @throws ServiceBroadcastException If the service could not be broadcasted.
     */
    public void broadcastService(EndpointInfo endpointInfo) throws ServiceBroadcastException
    {
        if (endpointInfo == null)
        {
            String errorMessage = "EndpointInfo cannot be null";
            logger.error(errorMessage);
            throw new ServiceBroadcastException(errorMessage);
        }

        String name = endpointInfo.getServiceName();
        int port = endpointInfo.getServicePort();
        String path = endpointInfo.getServicePath();
        String data = endpointInfo.getAdditionalData();

        ServiceInfo serviceInfo = createServiceInfo(name, port, path, data);

        if (services.containsValue(serviceInfo))
        {
            String errorMessage = String.format("Service with name '%s' already broadcasted", endpointInfo.getServiceName());
            logger.error(errorMessage);
            throw new ServiceBroadcastException(errorMessage);
        }

        ServiceBroadcaster broadcaster = getServiceBroadcaster(endpointInfo.getAddress());

        try
        {
            broadcaster.broadcastService(serviceInfo);
            services.put(endpointInfo.getServiceName(), serviceInfo);
            logger.info("Broadcasted service '{}' on interface '{}'", endpointInfo.getServiceName(), endpointInfo.getAddress());
        }

        catch (IOException ex)
        {
            String errorMessage = String.format("Could not broadcast service '%s' on interface '%s'", endpointInfo.getServiceName(), endpointInfo.getAddress());
            logger.error(errorMessage, ex);
            throw new ServiceBroadcastException(errorMessage, ex.getCause());
        }
    }


    /**
     * Stops broadcasting the service with the specified name.
     *
     * @param serviceName The name of the service to stop broadcasting.
     */
    public void stopBroadcastingService(String serviceName) throws ServiceBroadcastException
    {
        ServiceInfo serviceInfo = services.get(serviceName);

        if (serviceInfo == null)
        {
            String errorMessage = String.format("Service with name '%s' not broadcasted", serviceName);
            logger.error(errorMessage);
            throw new ServiceBroadcastException(errorMessage);
        }

        InetAddress interfaceAddress = serviceInfo.getInetAddresses()[0];
        ServiceBroadcaster broadcaster = serviceBroadcasters.get(interfaceAddress);

        broadcaster.stopBroadcastingService(serviceInfo);
        services.remove(serviceName);
        logger.info("Stopped broadcasting service '{}' on interface '{}'", serviceName, interfaceAddress);

        if (broadcaster.getBroadcastCounter() == 0)
        {
            broadcaster.destroy();
            serviceBroadcasters.remove(interfaceAddress);
            logger.info("Closed ServiceBroadcaster for interface '{}'", interfaceAddress);
        }
    }


    /**
     * Gets a list of all currently broadcasted services.
     *
     * @return A list of all currently broadcasted services.
     */
    public List<String> getBroadcastedServices()
    {
        return new ArrayList<>(services.keySet());
    }


    /**
     * Creates a ServiceInfo object based on the given EndpointInfo object.
     *
     * @param name           The name of the service.
     * @param port           The port on which the service is listening.
     * @param path           The path to the service.
     * @param additionalData Additional data to be added to the service.
     * @return A ServiceInfo object created based on the given EndpointInfo object.
     */
    private ServiceInfo createServiceInfo(String name, int port, String path, String additionalData)
    {
        ServiceInfo serviceInfo = ServiceInfo.create(SERVICE_TYPE, name, port, "Path=" + path);

        Optional<String> data = Optional.ofNullable(additionalData);
        data.ifPresent(d -> serviceInfo.setText(d.getBytes()));

        return serviceInfo;
    }


    /**
     * Gets the ServiceBroadcaster object for the given interface address. If the object does not exist, creates it.
     * Synchronized to prevent multiple threads from creating the same object at the same time.
     *
     * @param interfaceAddress The interface address to use.
     * @return The ServiceBroadcaster object for the given interface address.
     * @throws ServiceBroadcastException If a new ServiceBroadcaster object cannot be created.
     */
    private synchronized ServiceBroadcaster getServiceBroadcaster(InetAddress interfaceAddress) throws ServiceBroadcastException
    {
        if(interfaceAddress == null)
        {
            String errorMessage = "Interface address cannot be null";
            logger.error(errorMessage);
            throw new ServiceBroadcastException(errorMessage);
        }


        ServiceBroadcaster broadcaster = serviceBroadcasters.get(interfaceAddress);

        if (broadcaster == null)
        {
            try
            {
                broadcaster = new ServiceBroadcaster(interfaceAddress);
                serviceBroadcasters.put(interfaceAddress, broadcaster);
            }

            catch (IOException ex)
            {
                String errorMessage = String.format("Could not create service broadcaster for interface '%s'", interfaceAddress);
                logger.error(errorMessage, ex);
                throw new ServiceBroadcastException(errorMessage, ex.getCause());
            }
        }

        return broadcaster;
    }
}
