package MMS.EdgeRouter.WebSocketManager;

import net.maritimeconnectivity.pki.CertificateHandler;
import net.maritimeconnectivity.pki.PKIIdentity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.servlet.http.HttpServletRequest;
import java.security.cert.X509Certificate;
import java.util.UUID;


public class EdgeRouterEndpoint extends WebSocketAdapter
{
    private static final Logger logger = LogManager.getLogger(EdgeRouterEndpoint.class);
    private Session session;
    private final String uuid;

    public EdgeRouterEndpoint(Server server)
    {
        super();
        this.uuid = String.valueOf(UUID.randomUUID());
    }


    @Override
    public void onWebSocketConnect(Session session)
    {
        super.onWebSocketConnect(session);

        HttpServletRequest request = (HttpServletRequest) session.getUpgradeRequest();
        SSLSession sslSession = (SSLSession) request.getAttribute("javax.servlet.request.ssl_session");

        try
        {
            if (sslSession != null && sslSession.getPeerCertificates() != null && sslSession.getPeerCertificates().length > 0)
            {
                X509Certificate cert = (X509Certificate) sslSession.getPeerCertificates()[0];
                PKIIdentity identity = CertificateHandler.getIdentityFromCert(cert);
                logger.info("Connected to: \" + session.getRemoteAddress().getAddress().\nClient authenticated as: " + identity.getFirstName(), identity.getLastName(), identity.getCountry(), identity.getMrn());
                ConnectionHandler.getHandler().addSession(uuid, new SessionState(session, true));
            }

            else
            {
                logger.info("Connected to: \" + session.getRemoteAddress().getAddress().\nClient did not authenticate, connection will continue in anonymous mode.");
                ConnectionHandler.getHandler().addSession(uuid, new SessionState(session, false));
            }

            this.session = session;
        }

        catch (SSLPeerUnverifiedException e)
        {
            logger.error("An error occurred while performing TLS handshake during client connection from: " + session.getRemoteAddress().getAddress(), e);
            throw new RuntimeException(e);
        }
    }


    @Override
    public void onWebSocketText(String message)
    {
        super.onWebSocketText(message);
        logger.info("Received message: " + message);
    }


    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len)
    {
        super.onWebSocketBinary(payload, offset, len);
        logger.info("Received binary message");
    }


    @Override
    public void onWebSocketClose(int statusCode, String reason)
    {
        super.onWebSocketClose(statusCode, reason);
        ConnectionHandler.getHandler().removeSession(uuid);
        logger.info("Connection closed: " + statusCode + " " + reason);
    }


    @Override
    public void onWebSocketError(Throwable cause)
    {
        super.onWebSocketError(cause);
        ConnectionHandler.getHandler().removeSession(uuid);
        logger.error("Connection error: " + cause.getMessage());
    }

}