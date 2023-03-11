package MMS.EdgeRouter.Configuration;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;


public class TLSContextManager
{
    private final String keyStorePath = Config.getKeyStorePath();
    private final String keyStorePassword = Config.getKeyStorePassword();


    static
    {
        Security.addProvider(new BouncyCastleProvider());
    }


    public static SSLContext getTLSContext() throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException, KeyManagementException, UnrecoverableKeyException
    {
        SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
        TrustManager[] trustManagers = getTrustManagers();
        KeyManager[] keyManagers = getKeyManagers();
        sslContext.init(keyManagers, trustManagers, null);
        return sslContext;
    }


    private static KeyStore getKeystore() throws CertificateException, IOException, NoSuchAlgorithmException, KeyStoreException
    {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);
        return keyStore;
    }


    private static KeyStore getTrustStore() throws CertificateException, IOException, NoSuchAlgorithmException, KeyStoreException
    {
        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        trustStore.load(null, null);
        return trustStore;
    }


    private static TrustManager[] getTrustManagers() throws CertificateException, IOException, NoSuchAlgorithmException, KeyStoreException
    {
        KeyStore trustStore = getTrustStore();
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("PKIX");
        trustManagerFactory.init(trustStore);
        return trustManagerFactory.getTrustManagers();
    }


    private static KeyManager[] getKeyManagers() throws CertificateException, IOException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException
    {
        KeyStore keyStore = getKeystore();
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("PKIX");
        keyManagerFactory.init(keyStore, null);
        return keyManagerFactory.getKeyManagers();
    }
}
