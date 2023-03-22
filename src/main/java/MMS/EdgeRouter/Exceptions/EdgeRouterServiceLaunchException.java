package MMS.EdgeRouter.Exceptions;

public class EdgeRouterServiceLaunchException extends Exception
{
    public EdgeRouterServiceLaunchException(String message)
    {
        super(message);
    }

    public EdgeRouterServiceLaunchException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public EdgeRouterServiceLaunchException(Throwable cause)
    {
        super(cause);
    }
}
