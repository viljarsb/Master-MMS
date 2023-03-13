package MMS.EdgeRouter.WebSocketManager;


import net.maritimeconnectivity.pki.CertificateHandler;
import net.maritimeconnectivity.pki.PKIIdentity;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.servlet.http.HttpServletRequest;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import java.net.URI;

public class ConnectionHandler
{
    private final ConcurrentHashMap<URI, ConcurrentHashMap<Session, SessionState>> sessionState;
    private final ConcurrentHashMap<Session, SessionState> sessionState2;
    private static ConnectionHandler instance;


    private ConnectionHandler()
    {
        sessionState = new ConcurrentHashMap<>();
        sessionState2 = new ConcurrentHashMap<>();
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
                sessionState2.put(session, state);
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
        sessionState2.remove(session);
    }


    void unregisterClients(Server server)
    {
        URI uri = URI.create(server.getURI().toString());

        for(Session session : sessionState2.keySet())
        {
            if(sessionState2.get(session).connectedTo().equals(uri))
            {
                sessionState2.remove(session);
            }
        }
    }


    ArrayList<SessionState> getConnectedClients(Server server)
    {
        URI uri = URI.create(server.getURI().toString());
        ArrayList<SessionState> clients = new ArrayList<>();

        for(Session session : sessionState2.keySet())
        {
            if(sessionState2.get(session).connectedTo().equals(uri) && sessionState2.get(session).isConnected())
            {
                clients.add(sessionState2.get(session));
            }
        }

        return clients;
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
}
