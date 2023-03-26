package MMS.EdgeRouter.Exceptions;

public class WsEndpointDeploymentException extends Exception
{
    public WsEndpointDeploymentException(String message)
    {
        super(message);
    }

    public WsEndpointDeploymentException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public WsEndpointDeploymentException(Throwable cause)
    {
        super(cause);
    }
}
