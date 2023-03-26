package MMS.EdgeRouter.ServiceBroadcast;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;

/**
 * The {@code ServiceBroadcaster} class provides methods for broadcasting and stopping service advertisements
 * using JmDNS (Java Multicast DNS) zero-configuration networking technology.
 */
public class ServiceBroadcaster
{
    private final JmDNS jmdns;
    private int broadcastingCounter;


    /**
     * Constructs a new {@code ServiceBroadcaster} object with the given address.
     *
     * @param address The IP address used to create the JmDNS instance.
     * @throws IOException If an I/O error occurs while creating the JmDNS instance.
     */
    ServiceBroadcaster(String address) throws IOException
    {
        this.jmdns = JmDNS.create(address);
        this.broadcastingCounter = 0;
    }


    /**
     * Broadcasts the given service using the JmDNS instance.
     *
     * @param serviceInfo The service information to broadcast.
     * @throws IOException If an I/O error occurs while registering the service.
     */
    void broadcastService(ServiceInfo serviceInfo) throws IOException
    {
        this.jmdns.registerService(serviceInfo);
        this.broadcastingCounter++;
        System.out.println("Service broadcasted: " + serviceInfo.getName());
    }


    /**
     * Stops broadcasting the given service using the JmDNS instance.
     *
     * @param serviceInfo The service information to stop broadcasting.
     */
    void stopBroadcastingService(ServiceInfo serviceInfo)
    {
        this.jmdns.unregisterService(serviceInfo);
        this.broadcastingCounter--;
    }


    /**
     * Stops broadcasting all services currently being advertised by the JmDNS instance.
     */
    void stopBroadcastingAllServices()
    {
        this.jmdns.unregisterAllServices();
        this.broadcastingCounter = 0;
    }


    /**
     * Closes the JmDNS instance and releases any resources associated with it.
     *
     * @throws IOException If an I/O error occurs while closing the JmDNS instance.
     */
    void close() throws IOException
    {
        this.jmdns.unregisterAllServices();
        this.jmdns.close();
    }


    /**
     * Returns the current number of services being broadcasted.
     *
     * @return The broadcasting counter value.
     */
    int getBroadcastCounter()
    {
        return this.broadcastingCounter;
    }
}
