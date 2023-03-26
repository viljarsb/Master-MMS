package MMS.Client;

import MMS.Client.Connections.AnonymousConnection;
import MMS.Client.Connections.AuthenticatedConnection;
import MMS.Client.Connections.Connection;
import MMS.Client.Connections.DisconnectionReason;
import MMS.Client.Interfaces.ConnectionListener;
import MMS.Client.Interfaces.MessageListener;
import net.maritimeconnectivity.pki.CertificateHandler;
import net.maritimeconnectivity.pki.PKIIdentity;
import org.eclipse.jetty.websocket.api.BadPayloadException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;

import javax.servlet.http.HttpServletRequest;
import java.security.cert.X509Certificate;


/**
 * WsEndpoint is a WebSocket adapter that handles the lifecycle of a WebSocket
 * connection, including connection, message processing, and disconnection.
 * It works with ConnectionListener and MessageListener for handling the events
 * during the WebSocket connection.
 */
public class WsEndpoint extends WebSocketAdapter
{
    private final ConnectionListener connectionListener;
    private final MessageHandler messageHandler;
    private Connection connection;


    /**
     * Constructs a new WsEndpoint with the specified ConnectionListener and MessageListener.
     *
     * @param connectionListener the listener for handling connection events
     * @param messageListener    the listener for handling message events
     */
    public WsEndpoint(ConnectionListener connectionListener, MessageListener messageListener)
    {
        super();
        this.connectionListener = connectionListener;
        this.messageHandler = new MessageHandler(messageListener);
    }


    /**
     * Called when a WebSocket connection is established.
     * Sets up an authenticated or anonymous connection based on the client's certificate.
     *
     * @param session the WebSocket session
     */
    @Override
    public void onWebSocketConnect(Session session)
    {
        super.onWebSocketConnect(session);

        HttpServletRequest request = ((ServletUpgradeRequest) session.getUpgradeRequest()).getHttpServletRequest();
        X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");

        if (certs != null && certs.length > 0)
        {
            PKIIdentity identity = CertificateHandler.getIdentityFromCert(certs[0]);
            this.connection = new AuthenticatedConnection(session, identity.getMrn());
        }

        else
        {
            this.connection = new AnonymousConnection(session);
        }

        connectionListener.onConnectionEstablished(connection);
    }

    /**
     * Called when a WebSocket receives a text message. This method throws an error since binary messages are expected.
     *
     * @param message the received text message
     */
    @Override
    public void onWebSocketText(String message)
    {
        super.onWebSocketText(message);
        onWebSocketError(new BadPayloadException("Received text message, expected binary message"));
    }


    /**
     * Called when a WebSocket receives a binary message. Processes the message using the MessageHandler.
     *
     * @param payload the binary payload of the message
     * @param offset  the starting position of the payload
     * @param len     the length of the payload
     */
    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len)
    {
        super.onWebSocketBinary(payload, offset, len);
        messageHandler.processMessage(payload, offset, len);
    }


    /**
     * Called when a WebSocket connection is closed. Notifies the ConnectionListener with the disconnection reason.
     *
     * @param statusCode the WebSocket close status code
     * @param reason     the reason for disconnection
     */
    @Override
    public void onWebSocketClose(int statusCode, String reason)
    {
        super.onWebSocketClose(statusCode, reason);
        DisconnectionReason disconnectionReason = new DisconnectionReason(reason, statusCode);
        connectionListener.onConnectionLost(disconnectionReason);
    }


    /**
     * Called when a WebSocket encounters an error. Use the default implementation,
     * which will handle the error according to the WebSocket specification.
     *
     * @param cause the cause of the error
     */
    @Override
    public void onWebSocketError(Throwable cause)
    {
        super.onWebSocketError(cause);
    }


    /**
     * Returns the active WebSocket connection encapsulated in a connection object.
     *
     * @return the active WebSocket connection
     */
    Connection getConnection()
    {
        return connection;
    }
}
