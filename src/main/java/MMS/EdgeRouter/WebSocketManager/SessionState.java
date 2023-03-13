package MMS.EdgeRouter.WebSocketManager;

import net.maritimeconnectivity.pki.CertificateHandler;
import net.maritimeconnectivity.pki.PKIIdentity;
import org.eclipse.jetty.websocket.api.Session;

import java.net.URI;
import java.util.concurrent.CopyOnWriteArrayList;

public class SessionState
{
    private final Session session;
    private final Boolean authenticated;
    private Boolean connected;
    private final CopyOnWriteArrayList<String> unfetchedMessages;
    private final PKIIdentity identity;
    private final URI uri;


    public SessionState(Session session)
    {
        this.session = session;
        this.authenticated = false;
        this.connected = true;
        this.unfetchedMessages = new CopyOnWriteArrayList<>();
        this.identity = null;
        this.uri = session.getUpgradeRequest().getRequestURI();
    }


    public SessionState(Session session, PKIIdentity identity)
    {
        this.session = session;
        this.authenticated = true;
        this.connected = true;
        this.unfetchedMessages = new CopyOnWriteArrayList<>();
        this.identity = identity;
        this.uri = session.getUpgradeRequest().getRequestURI();
    }


    public Session getSession()
    {
        return session;
    }


    public Boolean isAuthenticated()
    {
        return authenticated;
    }


    public void setConnected(Boolean connected)
    {
        this.connected = connected;
    }


    public PKIIdentity getIdentity()
    {
        return identity;
    }

    public URI connectedTo()
    {
        return uri;
    }

    public boolean isConnected()
    {
        return connected;
    }
}
