package MMS.AgentV2;

import net.maritimeconnectivity.pki.CertificateHandler;
import org.eclipse.jetty.websocket.api.BadPayloadException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.List;

public class WsEndpoint extends WebSocketAdapter
{
    private final AgentAdapter adapter;
    private Session session;


    public WsEndpoint(AgentAdapter adapter)
    {
        super();
        this.adapter = adapter;
    }


    @Override
    public void onWebSocketConnect(Session session)
    {
        super.onWebSocketConnect(session);

        HttpServletRequest request = ((ServletUpgradeRequest) session.getUpgradeRequest()).getHttpServletRequest();
        X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");

        if (certs != null && certs.length > 0)
        {
            adapter.onAuthenticatedConnection(new AuthenticatedConnection(session));
        }

        else
        {
            adapter.onAnonymousConnection(new AnonymousConnection(session));
        }
    }


        @Override
        public void onWebSocketText (String message)
        {
            super.onWebSocketText(message);
            onWebSocketError(new BadPayloadException("Received text message, expected binary message"));
        }

        @Override
        public void onWebSocketBinary (byte[] payload, int offset, int len)
        {
            super.onWebSocketBinary(payload, offset, len);
            // handle binary message
        }

        @Override
        public void onWebSocketClose ( int statusCode, String reason )
        {
            super.onWebSocketClose(statusCode, reason);
            adapter.onDisconnect(reason, statusCode);
        }

        @Override
        public void onWebSocketError (Throwable cause)
        {
            super.onWebSocketError(cause);
        }
    }
