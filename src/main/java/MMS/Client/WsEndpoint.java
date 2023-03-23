package MMS.Client;

import MMS.Client.Connections.AnonymousConnection;
import MMS.Client.Connections.AuthenticatedConnection;
import MMS.Client.Connections.Connection;
import net.maritimeconnectivity.pki.CertificateHandler;
import net.maritimeconnectivity.pki.PKIIdentity;
import org.eclipse.jetty.websocket.api.BadPayloadException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;

import javax.servlet.http.HttpServletRequest;
import java.security.cert.X509Certificate;

public class WsEndpoint extends WebSocketAdapter
{
    private final AgentAdapter adapter;
    private final MessageHandler messageHandler;
    private Connection connection;


    public WsEndpoint(AgentAdapter adapter)
    {
        super();
        this.adapter = adapter;
        this.messageHandler = new MessageHandler(adapter);
    }


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
            adapter.onAuthenticatedConnectionDefault((AuthenticatedConnection) this.connection);
        }

        else
        {
            this.connection = new AnonymousConnection(session);
            adapter.onAnonymousConnectionDefault((AnonymousConnection) this.connection);
        }
    }


    @Override
    public void onWebSocketText(String message)
    {
        super.onWebSocketText(message);
        onWebSocketError(new BadPayloadException("Received text message, expected binary message"));
    }


    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len)
    {
        super.onWebSocketBinary(payload, offset, len);
        messageHandler.processMessage(payload, offset, len);
    }


    @Override
    public void onWebSocketClose(int statusCode, String reason)
    {
        super.onWebSocketClose(statusCode, reason);
        adapter.onDisconnectDefault(reason, statusCode);
    }


    @Override
    public void onWebSocketError(Throwable cause)
    {
        // If the connection is null, the connection has not been established yet, we let the default handler handle the other errors.
        if(this.connection == null)
        {
            adapter.onConnectionErrorDefault(cause.getMessage(), cause);
        }

        else
        {
            super.onWebSocketError(cause);
        }
    }


    Connection getConnection()
    {
        return connection;
    }
}
