package MMS.EdgeRouter.ServiceRegistry;

import org.eclipse.jetty.util.ssl.SslContextFactory;

/**
 * TLSConfiguration represents a configuration object for configuring TLS connections for the Edge Router.
 */
public class TLSConfiguration
{
    private final String keystorePath;
    private final String keystorePassword;
    private final String truststorePath;
    private final String truststorePassword;


    /**
     * Creates a new TLSConfiguration object with the specified keystore and truststore parameters.
     *
     * @param keystorePath       The path to the keystore file.
     * @param keystorePassword   The password to access the keystore file.
     * @param truststorePath     The path to the truststore file.
     * @param truststorePassword The password to access the truststore file.
     */
    public TLSConfiguration(String keystorePath, String keystorePassword, String truststorePath, String truststorePassword)
    {
        this.keystorePath = keystorePath;
        this.keystorePassword = keystorePassword;
        this.truststorePath = truststorePath;
        this.truststorePassword = truststorePassword;
    }


    /**
     * Returns the path to the keystore file.
     *
     * @return The path to the keystore file.
     */
    public String getKeystorePath()
    {
        return keystorePath;
    }


    /**
     * Returns the password to access the keystore file.
     *
     * @return The password to access the keystore file.
     */
    public String getKeystorePassword()
    {
        return keystorePassword;
    }


    /**
     * Returns the path to the truststore file.
     *
     * @return The path to the truststore file.
     */
    public String getTruststorePath()
    {
        return truststorePath;
    }


    /**
     * Returns the password to access the truststore file.
     *
     * @return The password to access the truststore file.
     */
    public String getTruststorePassword()
    {
        return truststorePassword;
    }


    /**
     * Returns a new instance of SslContextFactory.Server with the configured keystore and truststore parameters.
     * It is configured to use TLSv1.3 and PKIX algorithms, with wanted client authentication, but not required.
     *
     * @return A new instance of SslContextFactory.Server with the configured keystore and truststore parameters.
     */
    public SslContextFactory.Server getTLSContextFactory()
    {
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();

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
        sslContextFactory.setWantClientAuth(true);

        return sslContextFactory;
    }
}
