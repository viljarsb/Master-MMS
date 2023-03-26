package MMS.EdgeRouter.Exceptions;

public class ServiceRegistrationException extends Exception
{
    public ServiceRegistrationException()
    {
        super("Service registration exception");
    }


    public ServiceRegistrationException(String message)
    {
        super(message);
    }


    public ServiceRegistrationException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
