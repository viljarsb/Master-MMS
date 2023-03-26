package MMS.Client.Exceptions;

public class InvalidSubjectException extends Exception
{
    public InvalidSubjectException(String message)
    {
        super(message);
    }

    public InvalidSubjectException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public InvalidSubjectException(Throwable cause)
    {
        super(cause);
    }
}
