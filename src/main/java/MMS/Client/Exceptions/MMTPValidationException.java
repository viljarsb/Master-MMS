package MMS.Client.Exceptions;

public class MMTPValidationException extends Exception
{
    public MMTPValidationException()
    {
        super("MMTP validation exception");
    }

    public MMTPValidationException(String message)
    {
        super(message);
    }

    public MMTPValidationException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
