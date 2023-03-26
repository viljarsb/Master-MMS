package MMS.Client.Interfaces;

import java.util.List;

/**
 * Interface used to inform the Agent of subscription events.
 */
public interface SubscribeListener
{
    void onSubscriptionSuccess(List<String> subjects);
    void onSubscriptionFailure(List<String> subjects, String reason, Throwable cause);
    void onSubscriptionRemoved(List<String> subjects);
    void onDirectMessageSubscriptionChanged(boolean subscribed);
    void onDirectMessageSubscriptionFailure(String reason, Throwable cause);
}
