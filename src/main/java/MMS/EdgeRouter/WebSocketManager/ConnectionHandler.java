package MMS.EdgeRouter.WebSocketManager;


import net.maritimeconnectivity.pki.CertificateHandler;
import net.maritimeconnectivity.pki.PKIIdentity;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.servlet.http.HttpServletRequest;
import java.security.cert.X509Certificate;
import java.util.concurrent.ConcurrentHashMap;

import java.net.URI;

public class ConnectionHandler
{
    private final ConcurrentHashMap<URI, ConcurrentHashMap<Session, SessionState>> sessionState;
    private static ConnectionHandler instance;


    private ConnectionHandler()
    {
        sessionState = new ConcurrentHashMap<>();
    }


    static ConnectionHandler getHandler()
    {
        if (instance == null)
        {
            instance = new ConnectionHandler();
        }
        return instance;
    }


    void registerClient(Session session)
    {
        HttpServletRequest request = (HttpServletRequest) session.getUpgradeRequest();
        URI uri = URI.create(request.getRequestURI());

        if (!sessionState.containsKey(uri))
        {
            throw new IllegalStateException("Server does not exist");
        }

        try
        {
            SSLSession sslSession = (SSLSession) request.getAttribute("javax.servlet.request.ssl_session");
            if (sslSession != null && sslSession.getPeerCertificates() != null && sslSession.getPeerCertificates().length > 0)
            {
                X509Certificate cert = (X509Certificate) sslSession.getPeerCertificates()[0];
                PKIIdentity identity = CertificateHandler.getIdentityFromCert(cert);
                SessionState state = new SessionState(session, identity);
                sessionState.get(uri).put(session, state);
            }

            else
            {
                SessionState state = new SessionState(session);
                sessionState.get(uri).put(session, state);
            }
        }


        catch (SSLPeerUnverifiedException e)
        {
            throw new RuntimeException(e);
        }
    }


    void unregisterClient(Session session)
    {
        HttpServletRequest request = (HttpServletRequest) session.getUpgradeRequest();
        URI uri = URI.create(request.getRequestURI());

        if (!sessionState.containsKey(uri))
        {
            throw new IllegalStateException("Server does not exist");
        }

        if (!sessionState.get(uri).containsKey(session))
        {
            throw new IllegalStateException("Session does not exist");
        }

        sessionState.get(uri).remove(session);
    }


    SessionState getClientState(Session session)
    {
        HttpServletRequest request = (HttpServletRequest) session.getUpgradeRequest();
        URI uri = URI.create(request.getRequestURI());

        if (!sessionState.containsKey(uri))
        {
            throw new IllegalStateException("Server does not exist");
        }

        if (!sessionState.get(uri).containsKey(session))
        {
            throw new IllegalStateException("Session does not exist");
        }

        return sessionState.get(uri).get(session);
    }


    void addServer(URI uri) throws IllegalStateException
    {
        if (sessionState.containsKey(uri))
        {
            throw new IllegalStateException("Server already exists");
        }

        sessionState.put(uri, new ConcurrentHashMap<>());
    }


    void removeServer(URI uri)
    {
        if (!sessionState.containsKey(uri))
        {
            throw new IllegalStateException("Server does not exist");
        }

        sessionState.get(uri).forEach((session, state) -> session.close(StatusCode.SHUTDOWN, "Server going down"));
        sessionState.remove(uri);
    }
}
