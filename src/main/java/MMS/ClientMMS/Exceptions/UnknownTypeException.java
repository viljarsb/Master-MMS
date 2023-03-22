package MMS.ClientMMS.Exceptions;

public class UnknownTypeException extends Exception
{
    public UnknownTypeException(String message)
    {
        super(message);
    }

    public UnknownTypeException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public UnknownTypeException(Throwable cause)
    {
        super(cause);
    }
}
