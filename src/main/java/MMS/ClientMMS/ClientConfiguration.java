package MMS.ClientMMS;

import MMS.ClientMMS.Exceptions.SecuritySetupException;

import java.security.cert.Certificate;

public class ClientConfiguration
{
    private final String keyStorePath;
    private final String keyStorePassword;

    private final String trustStorePath;
    private final String trustStorePassword;

    private final String certificateStorePath;
    private final String certificateStorePassword;

    private final SMMPCallback callback;


    private ClientConfiguration(ConfigBuilder builder)
    {
        this.keyStorePath = builder.keyStorePath;
        this.keyStorePassword = builder.keyStorePassword;
        this.trustStorePath = builder.trustStorePath;
        this.trustStorePassword = builder.trustStorePassword;
        this.certificateStorePath = builder.certificateStorePath;
        this.certificateStorePassword = builder.certificateStorePassword;
        this.callback = builder.callback;
    }


    public String getKeyStorePath()
    {
        return keyStorePath;
    }


    public String getKeyStorePassword()
    {
        return keyStorePassword;
    }


    public String getTrustStorePath()
    {
        return trustStorePath;
    }


    public String getTrustStorePassword()
    {
        return trustStorePassword;
    }


    public String getCertificateStorePath()
    {
        return certificateStorePath;
    }


    public String getCertificateStorePassword()
    {
        return certificateStorePassword;
    }


    public SMMPCallback getCallback()
    {
        return callback;
    }


    public static class ConfigBuilder
    {
        private String keyStorePath;
        private String keyStorePassword;

        private String trustStorePath;
        private String trustStorePassword;

        private String certificateStorePath;
        private String certificateStorePassword;

        private SMMPCallback callback;


        public ConfigBuilder setKeyStore(String keyStorePath, String keyStorePassword)
        {
            this.keyStorePath = keyStorePath;
            this.keyStorePassword = keyStorePassword;
            return this;
        }


        public ConfigBuilder setTrustStore(String trustStorePath, String trustStorePassword)
        {
            this.trustStorePath = trustStorePath;
            this.trustStorePassword = trustStorePassword;
            return this;
        }


        public ConfigBuilder setCertificateStore(String certificateStorePath, String certificateStorePassword)
        {
            this.certificateStorePath = certificateStorePath;
            this.certificateStorePassword = certificateStorePassword;
            return this;
        }


        public ConfigBuilder setCallback(SMMPCallback callback)
        {
            this.callback = callback;
            return this;
        }


        public ClientConfiguration build()
        {
            return new ClientConfiguration(this);
        }
    }
}
