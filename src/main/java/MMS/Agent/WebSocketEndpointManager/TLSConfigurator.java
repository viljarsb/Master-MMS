package MMS.Agent.WebSocketEndpointManager;

import MMS.Agent.TLSConfig;
import org.eclipse.jetty.util.ssl.SslContextFactory;

/**
 * The {@code TLSConfigurator} class provides a convenient way to configure and manage
 * Transport Layer Security (TLS) and Mutual Transport Layer Security (mTLS) settings
 * for the WebSocket endpoint manager.
 * <p>
 * This class uses the Jetty {@code SslContextFactory.Client} to configure
 * the SSL/TLS settings.
 */
public class TLSConfigurator
{
    private String keystorePath = "C:\\Users\\Viljar\\IdeaProjects\\Master-MMS\\src\\main\\java\\TestIdentities\\keystore-test-viljar";
    private String keystorePassword = "8r91fpin885elh46aju8q0do6f";
    private String truststorePath = "C:\\Users\\Viljar\\IdeaProjects\\Master-MMS\\src\\main\\java\\TestIdentities\\truststore-root-ca";
    private String truststorePassword = "changeit";

    public TLSConfigurator(TLSConfig config)
    {
        this.keystorePath = config.getKeystorePath();
        this.keystorePassword = config.getKeystorePassword();
        this.truststorePath = config.getTruststorePath();
        this.truststorePassword = config.getTruststorePassword();
    }

    /**
     * Returns the path of the keystore.
     *
     * @return keystorePath the path of the keystore as a String
     */
    public String getKeystorePath()
    {
        return keystorePath;
    }


    /**
     * Sets the path of the keystore.
     *
     * @param keystorePath the path of the keystore as a String
     */
    public void setKeystorePath(String keystorePath)
    {
        this.keystorePath = keystorePath;
    }


    /**
     * Returns the password of the keystore.
     *
     * @return keystorePassword the password of the keystore as a String
     */
    public String getKeystorePassword()
    {
        return keystorePassword;
    }


    /**
     * Sets the password of the keystore.
     *
     * @param keystorePassword the password of the keystore as a String
     */
    public void setKeystorePassword(String keystorePassword)
    {
        this.keystorePassword = keystorePassword;
    }


    /**
     * Returns the path of the truststore.
     *
     * @return truststorePath the path of the truststore as a String
     */
    public String getTruststorePath()
    {
        return truststorePath;
    }


    /**
     * Sets the path of the truststore.
     *
     * @param truststorePath the path of the truststore as a String
     */
    public void setTruststorePath(String truststorePath)
    {
        this.truststorePath = truststorePath;
    }


    /**
     * Returns the password of the truststore.
     *
     * @return truststorePassword the password of the truststore as a String
     */
    public String getTruststorePassword()
    {
        return truststorePassword;
    }


    /**
     * Sets the password of the truststore.
     *
     * @param truststorePassword the password of the truststore as a String
     */
    public void setTruststorePassword(String truststorePassword)
    {
        this.truststorePassword = truststorePassword;
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