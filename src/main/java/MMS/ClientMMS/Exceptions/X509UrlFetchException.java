package MMS.ClientMMS.Exceptions;

public class X509UrlFetchException extends Exception
{
    public X509UrlFetchException(String message)
    {
        super(message);
    }

    public X509UrlFetchException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public X509UrlFetchException(Throwable cause)
    {
        super(cause);
    }
}
