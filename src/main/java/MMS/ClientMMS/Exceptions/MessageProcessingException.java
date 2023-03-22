package MMS.ClientMMS.Exceptions;

public class MessageProcessingException extends Exception
{
    public MessageProcessingException(String message)
    {
        super(message);
    }

    public MessageProcessingException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public MessageProcessingException(Throwable cause)
    {
        super(cause);
    }
}
