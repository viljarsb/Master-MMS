package MMS.Agent.Exceptions;

public class NotConnectedException extends Exception
{
    public NotConnectedException(String message)
    {
        super(message);
    }

    public NotConnectedException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public NotConnectedException(Throwable cause)
    {
        super(cause);
    }
}
