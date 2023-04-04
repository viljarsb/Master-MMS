package MMS.Client.Interfaces;

import java.util.List;

public interface SubscribeCallback
{
    void onSubscriptionSuccess(List<String> subject);
    void onSubscriptionFailure(List<String> subject, String reason, Throwable cause);
    void onSubscriptionRemoved(List<String> subject);
    void onDirectMessageSubscriptionChanged(boolean subscribed);
    void onDirectMessageSubscriptionFailure(String reason, Throwable cause);
}
