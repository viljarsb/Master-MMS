package MMS.Client.Exceptions;

public class DisconnectException extends Exception
{
    public DisconnectException(String message)
    {
        super(message);
    }

    public DisconnectException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public DisconnectException(Throwable cause)
    {
        super(cause);
    }
}
