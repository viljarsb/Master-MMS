package MMS.AgentV2;

import org.eclipse.jetty.util.ssl.SslContextFactory;

public class TLSConfig
{
    protected String trustStorePath;
    protected String trustStorePassword;

    public TLSConfig(String trustStorePath, String trustStorePassword)
    {
        this.trustStorePath = trustStorePath;
        this.trustStorePassword = trustStorePassword;
    }

    public TLSConfig()
    {
        this.trustStorePath = null;
        this.trustStorePassword = null;
    }

    public void setTrustStorePath(String trustStorePath)
    {
        this.trustStorePath = trustStorePath;
    }

    public void setTrustStorePassword(String trustStorePassword)
    {
        this.trustStorePassword = trustStorePassword;
    }

    public SslContextFactory.Client getTLSContextFactory()
    {
        SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();

        sslContextFactory.setTrustStorePath(trustStorePath);
        sslContextFactory.setTrustStorePassword(trustStorePassword);
        sslContextFactory.setTrustManagerFactoryAlgorithm("PKIX");
        sslContextFactory.setProvider("BC");
        sslContextFactory.setProtocol("TLSv1.3");
        sslContextFactory.setEndpointIdentificationAlgorithm(null);

        return sslContextFactory;
    }



}
