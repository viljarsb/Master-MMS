package MMS.Client.Exceptions;

public class ConnectException extends Exception
{
    public ConnectException(String message)
    {
        super(message);
    }

    public ConnectException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ConnectException(Throwable cause)
    {
        super(cause);
    }
}
