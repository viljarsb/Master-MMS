package MMS.EdgeRouter.WebsocketServerManager;

import MMS.EdgeRouter.Config;
import org.eclipse.jetty.util.ssl.SslContextFactory;


public class TLSConfigurator
{
    public static SslContextFactory configureTLS()
    {
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server(); // only for testing

        sslContextFactory.setKeyStorePath(Config.getKeystorePath());
        sslContextFactory.setKeyStorePassword(Config.getKeystorePassword());
        sslContextFactory.setKeyManagerPassword(Config.getKeystorePassword());


        sslContextFactory.setTrustStorePath(Config.getTruststorePath());
        sslContextFactory.setTrustStorePassword(Config.getTruststorePassword());

        sslContextFactory.setKeyManagerFactoryAlgorithm("SunX509");
        sslContextFactory.setTrustManagerFactoryAlgorithm("SunX509");
        sslContextFactory.setProtocol("TLSv1.3");
        sslContextFactory.setWantClientAuth(true);

        return sslContextFactory;
    }
}
