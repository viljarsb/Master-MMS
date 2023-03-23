package MMS.Client.Exceptions;

public class AgentInitException extends Exception
{
    public AgentInitException(String message)
    {
        super(message);
    }

    public AgentInitException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public AgentInitException(Throwable cause)
    {
        super(cause);
    }
}
