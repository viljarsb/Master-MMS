package MMS.EdgeRouter.Exceptions;

public class ServiceBroadcasterCreationException extends Exception
{
    public ServiceBroadcasterCreationException(String message)
    {
        super(message);
    }

    public ServiceBroadcasterCreationException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ServiceBroadcasterCreationException(Throwable cause)
    {
        super(cause);
    }
}
