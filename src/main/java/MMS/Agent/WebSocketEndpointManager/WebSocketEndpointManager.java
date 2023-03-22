package MMS.Agent.WebSocketEndpointManager;

import MMS.Agent.Exceptions.ConnectException;
import MMS.Agent.Exceptions.DisconnectException;
import MMS.Agent.Exceptions.MessageSendingException;
import MMS.Agent.Exceptions.NotConnectedException;
import MMS.Agent.AgentCallback;
import MMS.Agent.ServiceDiscoveryListner.RouterInfo;
import net.maritimeconnectivity.pki.CertificateHandler;
import net.maritimeconnectivity.pki.PKIIdentity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;

/**
 * This class represents a WebSocket endpoint manager, which is responsible for managing the WebSocket
 * connections to a remote server, including connecting, disconnecting, and sending messages.
 * It supports both anonymous connections and authenticated connections with mutual TLS (mTLS).
 * It uses a {@link ConnectionHandler} to manage the actual WebSocket connections and a {@link TLSConfigurator}
 * to configure TLS and mTLS settings.
 */
public class WebSocketEndpointManager
{
    private static final Logger logger = LogManager.getLogger(WebSocketEndpointManager.class);

    private final ConnectionHandler connectionHandler;
    private final TLSConfigurator tlsConfigurator;


    /**
     * Creates a new WebSocketEndpointManager with the given message callback.
     *
     * @param callback The {@link AgentCallback} instance to be used for handling incoming messages.
     */
    public WebSocketEndpointManager(AgentCallback callback, TLSConfigurator tlsConfigurator)
    {
        this.connectionHandler = new ConnectionHandler(callback);
        this.tlsConfigurator = tlsConfigurator;
    }


    /**
     * Connects to the WebSocket server anonymously using the provided router information.
     *
     * @param routerInfo The {@link RouterInfo} containing the server's URI.
     * @throws ConnectException If the connection attempt fails.
     */
    public void connectAnonymously(RouterInfo routerInfo, TLSConfigurator tlsConfigurator) throws ConnectException
    {
        if(tlsConfigurator.getTruststorePath() == null || tlsConfigurator.getTruststorePassword() == null)
            throw new ConnectException("Truststore path and password must be set to connect");

        SslContextFactory tlsContext = tlsConfigurator.configureTLS();
        this.connectionHandler.connectAnonymously(routerInfo.getUri(), tlsContext);
    }


    /**
     * Connects to the WebSocket server using mutual TLS authentication with the provided router information.
     *
     * @param routerInfo The {@link RouterInfo} containing the server's URI.
     * @throws ConnectException If the connection attempt fails.
     * @return The clients MRN.
     */
    public String connectAuthenticated(RouterInfo routerInfo) throws ConnectException
    {
        if(tlsConfigurator.getTruststorePath() == null || tlsConfigurator.getTruststorePassword() == null)
            throw new ConnectException("Truststore path and password must be set to connect");

        if(tlsConfigurator.getKeystorePath() == null || tlsConfigurator.getKeystorePassword() == null)
            throw new ConnectException("Keystore path and password must be set to connect authenticated");

        SslContextFactory tlsContext = tlsConfigurator.configureMTLS();
        this.connectionHandler.connectAuthenticated(routerInfo.getUri(), tlsConfigurator.configureMTLS());

        try
        {
            String alias = tlsContext.getCertAlias();
            X509Certificate cert = (X509Certificate) tlsContext.getKeyStore().getCertificate(alias);
            PKIIdentity identity = CertificateHandler.getIdentityFromCert(cert);
            return identity.getMrn();
        }

        // This cant really happen, because the connection is established, so the keystore is loaded and the alias is valid.
        catch (KeyStoreException ex)
        {
            logger.error("Failed to get certificate alias", ex);
            throw new ConnectException("Failed to get certificate alias", ex);
        }
    }


    /**
     * Disconnects the WebSocket connection, if connected.
     *
     * @throws DisconnectException If the disconnection attempt fails.
     */
    public void disconnect() throws DisconnectException
    {
        this.connectionHandler.disconnect();
    }


    /**
     * Checks if the WebSocket connection is currently connected.
     *
     * @return true if connected, false otherwise.
     */
    public boolean isConnected()
    {
        return this.connectionHandler.isConnected();
    }


    /**
     * Sends a message to the remote WebSocket server. The message is sent using the current WebSocket connection.
     *
     * @param message The message to send, represented as a byte array, should be a valid MMTP protocol message.
     * @throws NotConnectedException   if the WebSocket connection is not currently established.
     * @throws MessageSendingException if there is an error while sending the message.
     */
    public void send(byte[] message) throws NotConnectedException, MessageSendingException
    {
        try
        {
            if (isConnected())
            {
                this.connectionHandler.sendMessage(message);
            }

            else
            {
                logger.warn("Cannot send message because the WebSocket connection is not connected");
                throw new NotConnectedException("Cannot send message because the WebSocket connection is not connected");
            }
        }

        catch (IOException ex)
        {
            logger.error("Failed to send message", ex);
            throw new MessageSendingException("Failed to send message", ex);
        }
    }
}
