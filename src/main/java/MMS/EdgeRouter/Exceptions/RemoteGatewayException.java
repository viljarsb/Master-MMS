package MMS.EdgeRouter.Exceptions;

public class RemoteGatewayException extends Exception
{
    public RemoteGatewayException(String message)
    {
        super(message);
    }

    public RemoteGatewayException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public RemoteGatewayException(Throwable cause)
    {
        super(cause);
    }
}
