package MMS.EdgeRouter.Exceptions;

public class ServiceUnregistrationException extends Exception
{
    public ServiceUnregistrationException(String message)
    {
        super(message);
    }

    public ServiceUnregistrationException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ServiceUnregistrationException(Throwable cause)
    {
        super(cause);
    }
}
