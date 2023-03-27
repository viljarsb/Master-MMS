package MMS.EdgeRouter.Exceptions;

public class NoSuchServiceException extends Exception
{
    public NoSuchServiceException(String message)
    {
        super(message);
    }

    public NoSuchServiceException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public NoSuchServiceException(Throwable cause)
    {
        super(cause);
    }
}
