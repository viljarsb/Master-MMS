package MMS.ClientMMS.Exceptions;


public class SetupException extends Exception
{
    public SetupException(String message)
    {
        super(message);
    }

    public SetupException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public SetupException(Throwable cause)
    {
        super(cause);
    }
}
