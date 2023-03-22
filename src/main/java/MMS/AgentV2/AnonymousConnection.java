package MMS.AgentV2;

import org.eclipse.jetty.websocket.api.Session;

import java.util.List;

public class AnonymousConnection extends Connection
{

    public AnonymousConnection(Session session)
    {
        super(session);
    }


    public void subscribe(String subject)
    {

    }

    public void subscribe(List<String> subjects)
    {

    }

    public void unsubscribe(String subject)
    {

    }

    public void unsubscribe(List<String> subjects)
    {

    }


}
