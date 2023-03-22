package MMS.Agent.Exceptions;

public class InvalidMrnException extends Exception
{
    public InvalidMrnException(String message)
    {
        super(message);
    }

    public InvalidMrnException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public InvalidMrnException(Throwable cause)
    {
        super(cause);
    }
}
