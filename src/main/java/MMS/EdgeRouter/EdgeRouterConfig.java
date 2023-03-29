package MMS.EdgeRouter;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

public class EdgeRouterConfig
{
    private final String name;
    private final int port;
    private final String path;
    private final InetAddress ip;
    private final Boolean broadcast;

    private final int maxConnections;
    private final int maxRequestsPerSec;
    private final long delayMs;
    private final long throttleMs;
    private final boolean trackSessions;
    private final Set<String> whitelist;
    private final int tooManyCode;

    private final TLSConfiguration tlsConfiguration;


    private EdgeRouterConfig(Builder builder)
    {
        this.name = builder.name;
        this.port = Integer.parseInt(builder.port);
        this.path = builder.path;
        this.ip = builder.ip;
        this.broadcast = builder.broadcast;
        this.maxConnections = builder.maxConnections;
        this.maxRequestsPerSec = builder.maxRequestsPerSec;
        this.delayMs = builder.delayMs;
        this.throttleMs = builder.throttleMs;
        this.trackSessions = builder.trackSessions;
        this.whitelist = builder.whitelist;
        this.tooManyCode = builder.tooManyCode;

        this.tlsConfiguration = new TLSConfiguration(builder.keystorePath, builder.keystorePassword, builder.truststorePath, builder.truststorePassword);
    }

    public String getName()
    {
        return name;
    }


    public int getPort()
    {
        return port;
    }


    public String getPath()
    {
        return path;
    }


    public InetAddress getIp()
    {
        return ip;
    }


    public Boolean getBroadcast()
    {
        return broadcast;
    }


    public TLSConfiguration getTlsConfiguration()
    {
        return tlsConfiguration;
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


    public static class Builder
    {
        private final InetAddress ip = InetAddress.getByName("0.0.0.0");
        private final String name = ip.getHostName();
        private final String port = "8080";
        private final String path = "/";
        private final Boolean broadcast = true;

        private int maxConnections = 255;
        private int maxRequestsPerSec = 100;
        private long delayMs = 100;
        private long throttleMs = 1000;
        private boolean trackSessions = true;
        private Set<String> whitelist = new HashSet<>();
        private int tooManyCode = 429;


        private final String keystorePath;
        private final String keystorePassword;
        private final String truststorePath;
        private final String truststorePassword;


        public Builder(String keystorePath, String keystorePassword, String truststorePath, String truststorePassword) throws UnknownHostException
        {
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


        public EdgeRouterConfig build()
        {
            return new EdgeRouterConfig(this);
        }
    }
}
