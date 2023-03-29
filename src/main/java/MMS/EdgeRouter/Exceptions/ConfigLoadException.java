package MMS.EdgeRouter.Exceptions;

public class ConfigLoadException extends Exception
{
    public ConfigLoadException(String message)
    {
        super(message);
    }

    public ConfigLoadException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ConfigLoadException(Throwable cause)
    {
        super(cause);
    }
}
