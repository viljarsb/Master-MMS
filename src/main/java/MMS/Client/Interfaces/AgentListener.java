package MMS.Client.Interfaces;

import MMS.Client.AgentStatus;
import MMS.Client.Connections.DisconnectionReason;
import MMS.Client.ServiceDiscovery.RouterInfo;

import java.time.Instant;
import java.util.List;


/**
 * AgentListener is an abstract class that provides a set of methods for handling
 * connection and message events. It is used by the Agent class to notify the
 * application about the events, applications can extend this class and override
 * the methods to handle the events accordingly.
 */
public abstract class AgentListener
{
    // Connection-related methods
    public abstract void onConnect(AgentStatus status);
    public abstract void onDisconnect(DisconnectionReason reason);
    public abstract void onHandshakeError(DisconnectionReason reason);

    // Message-related methods
    public abstract void onDirectMessage(String messageId, String sender, Instant expires, byte[] message);
    public abstract void onSubjectCastMessage(String messageId, String sender, String subject, Instant expires, byte[] message);

    // Subscription-related methods
    public abstract void onSubscriptionSuccess(List<String> subject);
    public abstract void onSubscriptionFailure(List<String> subject, String reason, Throwable cause);
    public abstract void onSubscriptionRemoved(List<String> subject);
    public abstract void onDirectMessageSubscriptionChanged(boolean subscribed);
    public abstract void onDirectMessageSubscriptionFailure(String reason, Throwable cause);
}
