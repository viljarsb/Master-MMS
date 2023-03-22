package MMS.AgentV2;

import org.eclipse.jetty.websocket.api.Session;

public abstract class Connection
{
    private final Session session;

    public Connection(Session session)
    {
        this.session = session;
    }


    public boolean isConnected()
    {
        return session.isOpen();
    }
}
