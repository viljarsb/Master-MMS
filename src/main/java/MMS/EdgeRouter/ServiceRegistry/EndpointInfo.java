package MMS.EdgeRouter.ServiceRegistry;


import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * {@code EndpointInfo} is a utility class that holds the configuration for the Edge Router Service.
 * It is a simple POJO with getters and a builder to create it, as it can be quite complex to create.
 */
public class EndpointInfo
{
    private final String serviceName;
    private final String servicePath;
    private final int servicePort;
    private final Boolean isPublic;
    private final String address;
    private final int maxConnections;
    private final int maxRequestsPerSec;
    private final long delayMs;
    private final long throttleMs;
    private final boolean trackSessions;
    private final Set<String> whitelist;
    private final int tooManyCode;
    private final String additionalData;

    private TLSConfiguration tlsConfiguration;


    /**
     * Private constructor for the EndpointInfo class.
     * Initializes fields with the values provided by the builder.
     *
     * @param builder The {@link Builder} object containing values for the fields.
     */
    private EndpointInfo(Builder builder)
    {
        this.serviceName = builder.serviceName;
        this.servicePath = builder.servicePath;
        this.servicePort = builder.servicePort;
        this.isPublic = builder.isPublic;
        this.address = getLocalIPAddress(); // for testing purposes.
        this.maxConnections = builder.maxConnections;
        this.maxRequestsPerSec = builder.maxRequestsPerSec;
        this.delayMs = builder.delayMs;
        this.throttleMs = builder.throttleMs;
        this.trackSessions = builder.trackSessions;
        this.whitelist = builder.whitelist;
        this.tooManyCode = builder.tooManyCode;
        this.additionalData = builder.additionalData;

        this.tlsConfiguration = new TLSConfiguration(builder.keystorePath, builder.keystorePassword, builder.truststorePath, builder.truststorePassword);
    }


    public TLSConfiguration getTlsConfiguration()
    {
        return tlsConfiguration;
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


    public String getAddress()
    {
        return address;
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


    public String getAdditionalData()
    {
        return additionalData;
    }


    @Override
    public boolean equals(Object o)
    {
        if (o == this)
        {
            return true;
        }

        if (!(o instanceof EndpointInfo endpointInfo))
        {
            return false;
        }

        if (Objects.equals(this.serviceName, ((EndpointInfo) o).serviceName))
        {
            return true;
        }

        return endpointInfo.servicePath.equals(this.servicePath) &&
                endpointInfo.servicePort == this.servicePort &&
                endpointInfo.address.equals(this.address);
    }


    /**
     * The {@code Builder} class helps in constructing a new {@code EndpointInfo} object.
     */
    public static class Builder
    {
        private final String serviceName;
        private final String servicePath;
        private final int servicePort;
        private final Boolean isPublic;
        private int maxConnections = 255;
        private int maxRequestsPerSec = 100;
        private long delayMs = 100;
        private long throttleMs = 1000;
        private boolean trackSessions = true;
        private Set<String> whitelist = new HashSet<>();
        private int tooManyCode = 429;
        private String additionalData;


        private final String keystorePath;
        private final String keystorePassword;
        private final String truststorePath;
        private final String truststorePassword;


        /**
         * Constructs a new {@code Builder} object with the required fields.
         *
         * @param serviceName        The service name.
         * @param servicePath        The service path.
         * @param servicePort        The service port.
         * @param isPublic           Whether the service is public or not.
         * @param keystorePath       The path to the keystore.
         * @param keystorePassword   The password for the keystore.
         * @param truststorePath     The path to the truststore.
         * @param truststorePassword The password for the truststore.
         */
        public Builder(String serviceName, String servicePath, int servicePort, Boolean isPublic, String keystorePath, String keystorePassword, String truststorePath, String truststorePassword)
        {
            this.serviceName = serviceName;
            this.servicePath = servicePath;
            this.servicePort = servicePort;
            this.isPublic = isPublic;

            this.keystorePath = keystorePath;
            this.keystorePassword = keystorePassword;
            this.truststorePath = truststorePath;
            this.truststorePassword = truststorePassword;
        }


        public Builder setMaxConnections(int maxConnections)
        {
            this.maxConnections = maxConnections;
            return this;
        }


        public Builder setMaxRequestsPerSec(int maxRequestsPerSec)
        {
            this.maxRequestsPerSec = maxRequestsPerSec;
            return this;
        }


        public Builder setDelayMs(long delayMs)
        {
            this.delayMs = delayMs;
            return this;
        }


        public Builder setThrottleMs(long throttleMs)
        {
            this.throttleMs = throttleMs;
            return this;
        }


        public Builder setWhitelist(Set<String> whitelist)
        {
            this.whitelist = whitelist;
            return this;
        }


        public Builder setTrackSessions(boolean trackSessions)
        {
            this.trackSessions = trackSessions;
            return this;
        }


        public Builder setTooManyCode(int tooManyCode)
        {
            this.tooManyCode = tooManyCode;
            return this;
        }


        public Builder setAdditionalData(String additionalData)
        {
            this.additionalData = additionalData;
            return this;
        }


        /**
         * Builds a new {@code EndpointInfo} object using the values provided to the {@code Builder}.
         *
         * @return A new {@code EndpointInfo} object.
         */
        public EndpointInfo build()
        {
            return new EndpointInfo(this);
        }
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


    @Override
    public String toString()
    {
        return "Service Name: " + serviceName + ", Service Port: " + servicePort + ", Service Path: " + servicePath + ", Public: " + isPublic;
    }
}

