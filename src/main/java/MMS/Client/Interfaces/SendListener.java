package MMS.Client.Interfaces;


public interface SendListener
{
    void onSuccess(String messageID);
    void onFailure(Throwable cause);
}
