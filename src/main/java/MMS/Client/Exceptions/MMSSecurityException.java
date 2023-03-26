package MMS.Client.Exceptions;

public class MMSSecurityException extends Exception
{
    public MMSSecurityException()
    {
        super("Security exception");
    }

    public MMSSecurityException(String message)
    {
        super(message);
    }

    public MMSSecurityException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
