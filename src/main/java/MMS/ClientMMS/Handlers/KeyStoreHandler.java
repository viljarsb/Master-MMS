package MMS.ClientMMS.Handlers;

import MMS.ClientMMS.ClientConfiguration;
import MMS.ClientMMS.Exceptions.RemotePublicKeyException;
import MMS.ClientMMS.Exceptions.SecuritySetupException;
import MMS.ClientMMS.Exceptions.X509UrlFetchException;
import net.maritimeconnectivity.pki.CertificateHandler;
import net.maritimeconnectivity.pki.PKIIdentity;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.*;
import java.security.*;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Enumeration;

public class KeyStoreHandler
{
    private final KeyStore keyStore;
    private final KeyStore trustStore;
    private final KeyStore certificateStore;

    private final ClientConfiguration clientConfiguration;

    private ECPrivateKey privateKey;
    private ECPublicKey publicKey;
    private PKIIdentity pkiIdentity;


    public KeyStoreHandler(ClientConfiguration clientConfiguration) throws SecuritySetupException
    {
        this.clientConfiguration = clientConfiguration;

        String keyStorePath = clientConfiguration.getKeyStorePath();
        String keyStorePassword = clientConfiguration.getKeyStorePassword();
        String trustStorePath = clientConfiguration.getTrustStorePath();
        String trustStorePassword = clientConfiguration.getTrustStorePassword();
        String certificateStorePath = clientConfiguration.getCertificateStorePath();
        String certificateStorePassword = clientConfiguration.getCertificateStorePassword();

        try
        {
            this.keyStore = loadKeyStore(keyStorePath, keyStorePassword);
            this.trustStore = loadKeyStore(trustStorePath, trustStorePassword);
            this.certificateStore = loadKeyStore(certificateStorePath, certificateStorePassword);
            setupIdentity();
        }

        catch (Exception ex)
        {
            throw new SecuritySetupException("Failed to configure keystore", ex);
        }
    }


    private KeyStore loadKeyStore(String keyStorePath, String keyStorePassword) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException
    {
        KeyStore ks = KeyStore.getInstance("JKS");

        try (FileInputStream fis = new FileInputStream(keyStorePath))
        {
            ks.load(fis, keyStorePassword.toCharArray());
        }

        return ks;
    }


    private void setupIdentity() throws KeyStoreException
    {
        Enumeration<String> aliases = keyStore.aliases();
        boolean identityFound = false;

        while (aliases.hasMoreElements() && !identityFound)
        {
            String alias = aliases.nextElement();
            if (keyStore.isKeyEntry(alias))
            {
                try
                {
                    Key key = keyStore.getKey(alias, clientConfiguration.getKeyStorePassword().toCharArray());
                    if (key instanceof ECPrivateKey)
                    {
                        X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);
                        PublicKey publicKey = cert.getPublicKey();
                        if ("EC".equals(publicKey.getAlgorithm()))
                        {
                            PKIIdentity identity = CertificateHandler.getIdentityFromCert(cert);
                            if (identity.getMrn() != null)
                            {
                                identityFound = true;
                                this.privateKey = (ECPrivateKey) key;
                                this.publicKey = (ECPublicKey) publicKey;
                                this.pkiIdentity = identity;
                            }
                        }
                    }
                }
                catch (NoSuchAlgorithmException | UnrecoverableKeyException e)
                {
                    throw new KeyStoreException("Error retrieving EC key pair and certificate from keystore.", e);
                }
            }
        }

        if (!identityFound)
        {
            throw new KeyStoreException("No MCP identity found in keystore.");
        }
    }


    public ECPrivateKey getMyPrivateKey()
    {
        return privateKey;
    }


    public ECPublicKey getMyPublicKey()
    {
        return publicKey;
    }


    public PKIIdentity getMyPkiIdentity()
    {
        return pkiIdentity;
    }


    public ECPublicKey getRemotePublicKey(String MRN) throws RemotePublicKeyException, KeyStoreException
    {
        try
        {
            X509Certificate cert = (X509Certificate) certificateStore.getCertificate(MRN);
            CertificateHandler.verifyCertificateChain(cert, trustStore);
            PublicKey publicKey = cert.getPublicKey();
            if ("EC".equals(publicKey.getAlgorithm()))
            {
                return (ECPublicKey) publicKey;
            }
        }

        catch (CertPathValidatorException ex)
        {
            certificateStore.deleteEntry(MRN);

            try
            {
                certificateStore.deleteEntry(MRN);
            }

            catch (KeyStoreException ignored)
            {}

            throw new RemotePublicKeyException("Certificate chain validation failed.", ex);
        }

        catch (InvalidAlgorithmParameterException | CertificateException | KeyStoreException | NoSuchAlgorithmException ex)
        {
            throw new RemotePublicKeyException("Failed to retrieve remote public key.", ex);
        }

        return null;
    }


    public ECPublicKey getECPublicKeyFromX509URL(String x5u) throws X509UrlFetchException
    {
        try
        {
            OkHttpClient client = new okhttp3.OkHttpClient();
            Request request = new Request.Builder().url(x5u).build();
            Response response = client.newCall(request).execute();

            if(response.isSuccessful())
            {
                assert response.body() != null;
                String pemContent = response.body().string();
                X509Certificate certificate = CertificateHandler.getCertFromPem(pemContent);
                validateAndStoreCertificate(certificate, x5u);

                return (ECPublicKey) certificate.getPublicKey();
            }


            else
            {
                throw new X509UrlFetchException("Failed to fetch X.509 certificate from X5U URL: " + x5u + ". Response code: " + response.code());
            }
        }

        catch (IOException ex)
        {
            throw new X509UrlFetchException("Failed to fetch X.509 certificate from X5U URL: " + x5u, ex);
        }
    }


    private void validateAndStoreCertificate(X509Certificate certificate, String x5u) throws X509UrlFetchException
    {
        try
        {
            CertificateHandler.verifyCertificateChain(certificate, trustStore);
            PKIIdentity identity = CertificateHandler.getIdentityFromCert(certificate);
            certificateStore.setCertificateEntry(identity.getMrn(), certificate);
        }

        catch (CertPathValidatorException | InvalidAlgorithmParameterException | KeyStoreException | NoSuchAlgorithmException | CertificateException e)
        {
            throw new X509UrlFetchException("Failed to fetch X.509 certificate from X5U URL: " + x5u, e);
        }
    }
}
