package MMS.ClientMMS.Exceptions;

public class RemotePublicKeyException extends Exception
{
    public RemotePublicKeyException(String message)
    {
        super(message);
    }

    public RemotePublicKeyException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public RemotePublicKeyException(Throwable cause)
    {
        super(cause);
    }
}
