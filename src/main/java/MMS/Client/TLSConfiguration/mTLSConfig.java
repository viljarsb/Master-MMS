package MMS.Client.TLSConfiguration;

import org.eclipse.jetty.util.ssl.SslContextFactory;

public class mTLSConfig extends TLSConfig
{
    private String keyStorePath;
    private String keyStorePassword;

    public mTLSConfig(String trustStorePath, String trustStorePassword, String keyStorePath, String keyStorePassword)
    {
        super(trustStorePath, trustStorePassword);
        this.keyStorePath = keyStorePath;
        this.keyStorePassword = keyStorePassword;
    }

    public mTLSConfig()
    {
        super();
        this.keyStorePath = null;
        this.keyStorePassword = null;
    }

    public void setKeyStorePath(String keyStorePath)
    {
        this.keyStorePath = keyStorePath;
    }

    public void setKeyStorePassword(String keyStorePassword)
    {
        this.keyStorePassword = keyStorePassword;
    }

    @Override
    public SslContextFactory.Client getTLSContextFactory()
    {
        SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();

        sslContextFactory.setKeyStorePath(keyStorePath);
        sslContextFactory.setKeyStorePassword(keyStorePassword);
        sslContextFactory.setKeyManagerPassword(keyStorePassword);
        sslContextFactory.setTrustStorePath(super.trustStorePath);
        sslContextFactory.setTrustStorePassword(super.trustStorePassword);
        sslContextFactory.setKeyManagerFactoryAlgorithm("PKIX");
        sslContextFactory.setTrustManagerFactoryAlgorithm("PKIX");
        sslContextFactory.setProvider("BC");
        sslContextFactory.setProtocol("TLSv1.3");
        sslContextFactory.setEndpointIdentificationAlgorithm(null);
        sslContextFactory.setNeedClientAuth(true);

        return sslContextFactory;
    }
}
