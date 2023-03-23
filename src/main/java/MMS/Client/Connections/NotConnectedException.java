package MMS.Client.Connections;

public class NotConnectedException extends Exception
{
    public NotConnectedException()
    {
        super("Agent is not connected");
    }

    public NotConnectedException(String message)
    {
        super(message);
    }

    public NotConnectedException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
