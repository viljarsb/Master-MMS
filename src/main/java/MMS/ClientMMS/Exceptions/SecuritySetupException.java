package MMS.ClientMMS.Exceptions;

public class SecuritySetupException extends Exception
{
    public SecuritySetupException(String message)
    {
        super(message);
    }

    public SecuritySetupException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public SecuritySetupException(Throwable cause)
    {
        super(cause);
    }
}
