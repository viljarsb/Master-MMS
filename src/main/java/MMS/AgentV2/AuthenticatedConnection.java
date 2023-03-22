package MMS.AgentV2;

import org.eclipse.jetty.websocket.api.Session;

import java.time.Instant;

public class AuthenticatedConnection extends AnonymousConnection
{
    public AuthenticatedConnection(Session session)
    {
        super(session);
    }


    public void sendDirected(String destination, byte[] message, Instant expires)
    {

    }

    public void sendSubjectCast(String destination, byte[] message, Instant expires)
    {

    }

    public void subscribeToDM()
    {

    }


    public void unsubscribeFromDM()
    {

    }
}
