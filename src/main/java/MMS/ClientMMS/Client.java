package MMS.ClientMMS;

import MMS.Agent.Agent;
import MMS.Agent.Exceptions.*;
import MMS.Agent.ServiceDiscoveryListner.RouterInfo;
import MMS.Agent.TLSConfig;
import MMS.ClientMMS.Crypto.CryptoEngine;
import MMS.ClientMMS.Exceptions.SecuritySetupException;
import MMS.ClientMMS.Exceptions.SetupException;
import MMS.ClientMMS.Handlers.AckHandler;
import MMS.ClientMMS.Handlers.KeyStoreHandler;
import MMS.ClientMMS.Handlers.MessageHandler;
import MMS.SMMPMessages.SMMPMessage;
import com.google.protobuf.ByteString;
import net.maritimeconnectivity.pki.PKIIdentity;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

public class Client
{
    private final KeyStoreHandler keyStoreHandler;
    private final Agent agent;


    public Client(ClientConfiguration clientConfiguration) throws SetupException
    {
        try
        {
            validateConfiguration(clientConfiguration);
            TLSConfig tlsConfig = createTLSConfig(clientConfiguration);
            MessageHandler messageHandler = new MessageHandler();
            this.agent = new Agent(messageHandler, tlsConfig);
            this.keyStoreHandler = new KeyStoreHandler(clientConfiguration);

            SMMPCallback callback = clientConfiguration.getCallback();
            configureAckHandler(callback, messageHandler);
            configureMessageHandler(callback, messageHandler);
        }

        catch (Exception ex)
        {
            throw new SetupException("Failed to setup client", ex);
        }
    }


    private void validateConfiguration(ClientConfiguration clientConfiguration)
    {
        String keyStorePath = clientConfiguration.getKeyStorePath();
        String keyStorePassword = clientConfiguration.getKeyStorePassword();
        String trustStorePath = clientConfiguration.getTrustStorePath();
        String trustStorePassword = clientConfiguration.getTrustStorePassword();
        String certificateStorePath = clientConfiguration.getCertificateStorePath();
        String certificateStorePassword = clientConfiguration.getCertificateStorePassword();
        SMMPCallback callback = clientConfiguration.getCallback();

        if (keyStorePath == null || keyStorePath.isEmpty())
        {
            throw new IllegalArgumentException("KeyStorePath cannot be null or empty");
        }

        if (keyStorePassword == null || keyStorePassword.isEmpty())
        {
            throw new IllegalArgumentException("KeyStorePassword cannot be null or empty");
        }

        if (trustStorePath == null || trustStorePath.isEmpty())
        {
            throw new IllegalArgumentException("TrustStorePath cannot be null or empty");
        }

        if (trustStorePassword == null || trustStorePassword.isEmpty())
        {
            throw new IllegalArgumentException("TrustStorePassword cannot be null or empty");
        }

        if (certificateStorePath == null || certificateStorePath.isEmpty())
        {
            throw new IllegalArgumentException("CertificateStorePath cannot be null or empty");
        }

        if (certificateStorePassword == null || certificateStorePassword.isEmpty())
        {
            throw new IllegalArgumentException("CertificateStorePassword cannot be null or empty");
        }

        if (callback == null)
        {
            throw new IllegalArgumentException("Callback cannot be null");
        }
    }



    private TLSConfig createTLSConfig(ClientConfiguration clientConfiguration)
    {
        TLSConfig tlsConfig = new TLSConfig();
        tlsConfig.setKeystorePath(clientConfiguration.getKeyStorePath());
        tlsConfig.setKeystorePassword(clientConfiguration.getKeyStorePassword());
        tlsConfig.setTruststorePath(clientConfiguration.getTrustStorePath());
        tlsConfig.setTruststorePassword(clientConfiguration.getTrustStorePassword());
        return tlsConfig;
    }


    private void configureAckHandler(SMMPCallback callback, MessageHandler messageHandler)
    {
        AckHandler ackHandler = new AckHandler();
        ackHandler.setAgent(agent);
        ackHandler.setCallback(callback);
        ackHandler.setKeyStoreHandler(keyStoreHandler);
        messageHandler.setAckHandler(ackHandler);
    }


    private void configureMessageHandler(SMMPCallback callback, MessageHandler messageHandler)
    {
        messageHandler.setCallback(callback);
        messageHandler.setKeyStoreHandler(keyStoreHandler);
    }


    public List<RouterInfo> Discover()
    {
        return agent.discover();
    }


    public void connectAnonymously(RouterInfo routerInfo) throws ConnectException
    {
        agent.connectAnonymously(routerInfo);
    }


    public void connectAuthenticated(RouterInfo routerInfo) throws ConnectException
    {
        agent.connectAuthenticated(routerInfo);
    }


    public void disconnect() throws DisconnectException
    {
        agent.disconnect();
    }


    public void subscribe(String interest) throws InvalidSubjectException, NotConnectedException, MessageSendingException
    {
        agent.subscribe(interest);
    }


    public void subscribe(List<String> interests) throws InvalidSubjectException, NotConnectedException, MessageSendingException
    {
        agent.subscribe(interests);
    }


    public void unsubscribe(String interest) throws InvalidSubjectException, NotConnectedException, MessageSendingException
    {
        agent.unsubscribe(interest);
    }


    public void unsubscribe(List<String> interests) throws InvalidSubjectException, NotConnectedException, MessageSendingException
    {
        agent.unsubscribe(interests);
    }


    public void sendDirectMessage(String destination, byte[] message, Instant expirationTime, boolean encrypt, boolean requireAck) throws AnonymousConnectionException, NotConnectedException, MessageSendingException
    {
        try
        {
            PKIIdentity pkiIdentity = keyStoreHandler.getMyPkiIdentity();
            byte[] signature = CryptoEngine.sign(message, keyStoreHandler.getMyPrivateKey());
            String x5u = pkiIdentity.getUrl();

            SMMPMessage.Builder builder = SMMPMessage.newBuilder();
            builder.setX5U(x5u);
            builder.setPayload(ByteString.copyFrom(message));
            builder.setIsEncrypted(encrypt);
            builder.setSignature(ByteString.copyFrom(signature));
            builder.setRequiresAck(requireAck);
            SMMPMessage smmpMessage = builder.build();
            message = smmpMessage.toByteArray();
        }

        catch (Exception ex)
        {
            throw new MessageSendingException("Could not send message", ex);
        }


        agent.sendDirectMessage(destination, message, expirationTime);
    }


    public void sendSubjectCast(String subject, byte[] message, Instant expirationTime, boolean wantAck) throws AnonymousConnectionException, NotConnectedException, MessageSendingException
    {
        try
        {
            PKIIdentity pkiIdentity = keyStoreHandler.getMyPkiIdentity();
            byte[] signature = CryptoEngine.sign(message, keyStoreHandler.getMyPrivateKey());
            String x5u = pkiIdentity.getUrl();

            SMMPMessage.Builder builder = SMMPMessage.newBuilder();
            builder.setX5U(x5u);
            builder.setPayload(ByteString.copyFrom(message));
            builder.setIsEncrypted(false);
            builder.setSignature(ByteString.copyFrom(signature));
            builder.setRequiresAck(wantAck);
            SMMPMessage smmpMessage = builder.build();
            message = smmpMessage.toByteArray();
        }

        catch (Exception ex)
        {
            throw new MessageSendingException("Could not send message", ex);
        }


        agent.sendSubjectCastMessage(subject, message, expirationTime);
    }
}
