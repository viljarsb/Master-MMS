package MMS.EdgeRouter.Exceptions;

public class EdgeRouterInitException extends Exception
{
    public EdgeRouterInitException(String message)
    {
        super(message);
    }

    public EdgeRouterInitException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public EdgeRouterInitException(Throwable cause)
    {
        super(cause);
    }
}
