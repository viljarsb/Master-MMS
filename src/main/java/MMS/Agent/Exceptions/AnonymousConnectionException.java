package MMS.Agent.Exceptions;

public class AnonymousConnectionException extends Exception
{
    public AnonymousConnectionException(String message)
    {
        super(message);
    }

    public AnonymousConnectionException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public AnonymousConnectionException(Throwable cause)
    {
        super(cause);
    }
}
