package MMS.Agent;


import org.eclipse.jetty.util.ssl.SslContextFactory;

public class TLSConfig
{
    private String keystorePath;
    private String keystorePassword;
    private String truststorePath;
    private String truststorePassword;


    public void setKeystorePath(String keystorePath)
    {
        this.keystorePath = keystorePath;
    }


    public void setKeystorePassword(String keystorePassword)
    {
        this.keystorePassword = keystorePassword;
    }


    public void setTruststorePath(String truststorePath)
    {
        this.truststorePath = truststorePath;
    }


    public void setTruststorePassword(String truststorePassword)
    {
        this.truststorePassword = truststorePassword;
    }


    public String getKeystorePath()
    {
        return this.keystorePath;
    }


    public String getKeystorePassword()
    {
        return this.keystorePassword;
    }


    public String getTruststorePath()
    {
        return this.truststorePath;
    }


    public String getTruststorePassword()
    {
        return this.truststorePassword;
    }


    /**
     * Configures TLS settings using the truststore.
     *
     * @return an SslContextFactory.Client instance with the configured
     * TLS settings
     */
    public SslContextFactory.Client configureTLS()
    {
        SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();

        sslContextFactory.setTrustStorePath(truststorePath);
        sslContextFactory.setTrustStorePassword(truststorePassword);
        sslContextFactory.setTrustManagerFactoryAlgorithm("PKIX");
        sslContextFactory.setProvider("BC");
        sslContextFactory.setProtocol("TLSv1.3");
        sslContextFactory.setEndpointIdentificationAlgorithm(null);

        return sslContextFactory;
    }


    /**
     * Configures mTLS settings using both the keystore and truststore.
     *
     * @return an SslContextFactory.Client instance with the configured
     * mTLS settings
     */
    public SslContextFactory.Client configureMTLS()
    {
        SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();

        sslContextFactory.setKeyStorePath(keystorePath);
        sslContextFactory.setKeyStorePassword(keystorePassword);
        sslContextFactory.setKeyManagerPassword(keystorePassword);
        sslContextFactory.setTrustStorePath(truststorePath);
        sslContextFactory.setTrustStorePassword(truststorePassword);
        sslContextFactory.setKeyManagerFactoryAlgorithm("PKIX");
        sslContextFactory.setTrustManagerFactoryAlgorithm("PKIX");
        sslContextFactory.setProvider("BC");
        sslContextFactory.setProtocol("TLSv1.3");
        sslContextFactory.setEndpointIdentificationAlgorithm(null);
        sslContextFactory.setNeedClientAuth(true);

        return sslContextFactory;
    }
}