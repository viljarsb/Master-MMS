package MMS.EdgeRouter.WebSocketManager;


import java.util.concurrent.ConcurrentHashMap;

public class ConnectionHandler
{
    private final ConcurrentHashMap<String, SessionState> sessionState;
    private static ConnectionHandler instance;

    private ConnectionHandler()
    {
        sessionState = new ConcurrentHashMap<>();
    }


    public static ConnectionHandler getHandler()
    {
        if(instance == null)
        {
            instance = new ConnectionHandler();
        }

        return instance;
    }



    public ConcurrentHashMap<String, SessionState> getSessionState()
    {
        return sessionState;
    }


    public void addSession(String uuid, SessionState sessionState)
    {
        this.sessionState.put(uuid, sessionState);
    }


    public void removeSession(String uuid)
    {
        this.sessionState.remove(uuid);
    }


    public void clearSessions()
    {
        this.sessionState.clear();
    }
}
