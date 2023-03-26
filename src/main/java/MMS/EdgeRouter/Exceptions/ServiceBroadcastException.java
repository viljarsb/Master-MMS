package MMS.EdgeRouter.Exceptions;

public class ServiceBroadcastException extends Exception
{
    public ServiceBroadcastException(String message)
    {
        super(message);
    }

    public ServiceBroadcastException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ServiceBroadcastException(Throwable cause)
    {
        super(cause);
    }
}
