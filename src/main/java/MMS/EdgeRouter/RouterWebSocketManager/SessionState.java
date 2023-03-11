package MMS.EdgeRouter.RouterWebSocketManager;

import org.eclipse.jetty.websocket.api.Session;

import java.util.concurrent.CopyOnWriteArrayList;

public class SessionState
{
    private final Session session;
    private final Boolean authenticated;
    private final CopyOnWriteArrayList<String> unfetchedMessages;


    public SessionState(Session session, Boolean authenticated)
    {
        this.session = session;
        this.authenticated = authenticated;
        this.unfetchedMessages = new CopyOnWriteArrayList<>();
    }


    public Session getSession()
    {
        return session;
    }


    public Boolean isAuthenticated()
    {
        return authenticated;
    }


    public CopyOnWriteArrayList<String> getUnfetchedMessages()
    {
        return unfetchedMessages;
    }


    public void addMessage(String message)
    {
        unfetchedMessages.add(message);
    }
}
