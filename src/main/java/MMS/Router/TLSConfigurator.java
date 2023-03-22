package MMS.Router;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.security.*;
import java.security.cert.CertificateException;

public class TLSConfigurator
{
    private static String keystorePath = "";
    private static String keystorePassword = "";
    private static String truststorePath = "";
    private static String truststorePassword = "";


    public static ServerSocket getSecureSeverSocket(String inetAddress, int port) throws Exception
    {
        KeyManager[] keyManagers = getKeyManagers(keystorePath, keystorePassword);
        TrustManager[] trustManagers = getTrustManagers(truststorePath, truststorePassword);

        SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
        sslContext.init(keyManagers, trustManagers, null);

        SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
        ServerSocket serverSocket = sslServerSocketFactory.createServerSocket(port, 50, InetAddress.getByName(inetAddress));
        ((SSLServerSocket) serverSocket).setNeedClientAuth(true); // enable mTLS

        return serverSocket;
    }

    public static void setKeystorePath(String keystorePath)
    {
        TLSConfigurator.keystorePath = keystorePath;
    }


    public static void setKeystorePassword(String keystorePassword)
    {
        TLSConfigurator.keystorePassword = keystorePassword;
    }


    public static void setTruststorePath(String truststorePath)
    {
        TLSConfigurator.truststorePath = truststorePath;
    }


    public static void setTruststorePassword(String truststorePassword)
    {
        TLSConfigurator.truststorePassword = truststorePassword;
    }


    public static String getKeystorePath()
    {
        return keystorePath;
    }


    public static String getKeystorePassword()
    {
        return keystorePassword;
    }



    private static KeyManager[] getKeyManagers(String keystorePath, String keystorePassword) throws Exception
    {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (InputStream keyStoreStream = new FileInputStream(keystorePath))
        {
            keyStore.load(keyStoreStream, keystorePassword.toCharArray());
        }

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keystorePassword.toCharArray());
        return keyManagerFactory.getKeyManagers();
    }


    private static TrustManager[] getTrustManagers(String truststorePath, String truststorePassword) throws Exception
    {
        KeyStore trustStore = KeyStore.getInstance("JKS");
        try (InputStream trustStoreStream = new FileInputStream(truststorePath))
        {
            trustStore.load(trustStoreStream, truststorePassword.toCharArray());
        }

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        return trustManagerFactory.getTrustManagers();
    }
}
