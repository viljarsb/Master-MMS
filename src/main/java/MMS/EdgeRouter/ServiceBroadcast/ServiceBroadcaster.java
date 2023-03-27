package MMS.EdgeRouter.ServiceBroadcast;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The {@code ServiceBroadcaster} class provides methods for broadcasting and stopping service advertisements
 * using JmDNS (Java Multicast DNS) zero-configuration networking technology.
 */
class ServiceBroadcaster
{
    private final static Logger logger = LogManager.getLogger(ServiceBroadcaster.class);

    private final JmDNS jmdns;
    private final String address;
    private final AtomicInteger broadcastingCounter;


    /**
     * Constructs a new {@code ServiceBroadcaster} object with the given address.
     *
     * @param address The address to bind the JmDNS instance to.
     * @throws IOException If an I/O error occurs while creating the JmDNS instance.
     */
    ServiceBroadcaster(InetAddress address) throws IOException
    {
        this.jmdns = JmDNS.create(address);
        this.address = address.getHostAddress();
        this.broadcastingCounter = new AtomicInteger(0);
        logger.info("Service Broadcaster Initialized for address: {}", address.getHostAddress());
    }


    /**
     * Broadcasts the given service using the JmDNS instance.
     *
     * @param serviceInfo The service information to broadcast.
     * @throws IOException If an I/O error occurs while registering the service.
     */
    void broadcastService(ServiceInfo serviceInfo) throws IOException
    {
        jmdns.registerService(serviceInfo);
        broadcastingCounter.incrementAndGet();
        logger.info("Service '{}' broadcasted on address: {}", serviceInfo.getName(), address);
    }


    /**
     * Stops broadcasting the given service using the JmDNS instance.
     *
     * @param serviceInfo The service information to stop broadcasting.
     */
    void stopBroadcastingService(ServiceInfo serviceInfo)
    {
        jmdns.unregisterService(serviceInfo);
        broadcastingCounter.decrementAndGet();
        logger.info("Service '{}' stopped broadcasting on address: {}", serviceInfo.getName(), address);
    }


    /**
     * Returns the current number of services being broadcasted.
     *
     * @return The broadcasting counter value.
     */
    int getBroadcastCounter()
    {
        return broadcastingCounter.get();
    }


    /**
     * Closes the JmDNS instance and releases any resources associated with it.
     */
    void destroy()
    {
        broadcastingCounter.set(0);
        jmdns.unregisterAllServices();

        try
        {
            jmdns.close();
            logger.info("JmDNS instance closed for address: {}", address);
        }

        catch (IOException ex)
        {
            logger.debug("Error while closing JmDNS instance: " + ex.getMessage());
        }
    }
}
