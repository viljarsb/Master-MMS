package MMS.EdgeRouter.SubscriptionManager;


import MMS.EdgeRouter.WsManagement.ConnectionHandler;
import MMS.EdgeRouter.WsManagement.ConnectionState;
import net.maritimeconnectivity.pki.PKIIdentity;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * {@code SubscriptionManager} is a singleton class that manages subscriptions for messages and direct messages.
 * It maintains a concurrent map of subscribers for each subject and another for direct message recipients.
 */
public class SubscriptionManager
{
    private static SubscriptionManager instance;
    private final ConcurrentHashMap<String, Set<Session>> subscriptions;
    private final ConcurrentHashMap<String, Set<Session>> wantsDirectMessages;
    private final ConnectionHandler connectionHandler;
    private static final Comparator<Session> sessionComparator = Comparator.comparing(Session::hashCode);


    /**
     * Private constructor for the SubscriptionManager class.
     * Initializes the concurrent maps and obtains an instance of the ConnectionHandler.
     */
    private SubscriptionManager()
    {
        subscriptions = new ConcurrentHashMap<>();
        wantsDirectMessages = new ConcurrentHashMap<>();
        connectionHandler = ConnectionHandler.getInstance();
    }


    /**
     * Returns the singleton instance of the SubscriptionManager class.
     * If it doesn't exist, it creates a new instance.
     *
     * @return The singleton instance of SubscriptionManager.
     */
    public static synchronized SubscriptionManager getInstance()
    {
        if (instance == null)
        {
            instance = new SubscriptionManager();
        }

        return instance;
    }


    /**
     * Adds subscriptions for the specified subjects and the subscriber session.
     *
     * @param subject    The list of subjects to subscribe to.
     * @param subscriber The subscriber session.
     */
    public void addSubscription(List<String> subject, Session subscriber)
    {
        for (String s : subject)
        {
            subscriptions.putIfAbsent(s, new ConcurrentSkipListSet<>(sessionComparator));
            subscriptions.get(s).add(subscriber);
        }
    }


    /**
     * Removes the subscription for the specified subjects and the subscriber session.
     *
     * @param subject    The list of subjects to unsubscribe from.
     * @param subscriber The subscriber session.
     */
    public void removeSubscription(List<String> subject, Session subscriber)
    {
        subscriptions.get(subject).remove(subscriber);
    }



    /**
     * Adds a direct message subscription for the specified session.
     * Take note that a MRN can have multiple sessions, so we need to track the sessions.
     *
     * @param session The session to add a direct message subscription for.
     */
    public void addDirectMessageSubscription(Session session)
    {
        ConnectionState state = connectionHandler.getConnectionState(session);
        PKIIdentity identity = state.getIdentity();

        if (identity == null)
        {
            session.close(StatusCode.POLICY_VIOLATION, "Agent is not authenticated, breach of policy");
            return;
        }

        String MRN = identity.getMrn();
        wantsDirectMessages.putIfAbsent(MRN, new ConcurrentSkipListSet<>(sessionComparator));
        wantsDirectMessages.get(MRN).add(session);
    }


    /**
     * Removes the direct message subscription for the specified session.
     * Take note that a MRN can have multiple sessions, so we need to track the sessions.
     *
     * @param session The session to remove the direct message subscription from.
     */
    public void removeDirectMessageSubscription(Session session)
    {
        for (String MRN : wantsDirectMessages.keySet())
        {
            wantsDirectMessages.get(MRN).remove(session);
        }
    }


    /**
     * Returns a set of sessions subscribed for direct messages for the specified MRNs.
     *
     * @param MRNs A list of MRNs to get the direct message subscribers for.
     * @return A set of sessions subscribed for direct messages for the specified MRNs.
     */
    public Set<Session> getDirectMessageSubscribers(List<String> MRNs)
    {
        Set<Session> sessions = new HashSet<>();

        for (String MRN : MRNs)
        {
            sessions.addAll(wantsDirectMessages.get(MRN));
        }

        return sessions;
    }


    /**
     * Returns a set of sessions subscribed to the specified subject.
     *
     * @param subject The subject to get the subscribers for.
     * @return A set of sessions subscribed to the specified subject.
     */
    public Set<Session> getSubscribers(String subject)
    {
        return new HashSet<>(subscriptions.get(subject));
    }


    /**
     * Returns a set of all subjects available in the SubscriptionManager.
     *
     * @return A set of all subjects.
     */
    public Set<String> getAllSubjects()
    {
        return subscriptions.keySet();
    }


    /**
     * Returns a set of all MRNs available in the SubscriptionManager.
     *
     * @return A set of all MRNs.
     */
    public Set<String> getAllMRNs()
    {
        return wantsDirectMessages.keySet();
    }



    public HashMap<String, Integer> getSubscriptionNumbers()
    {
        HashMap<String, Integer> subscriptionNumbers = new HashMap<>();
        for (String subject : subscriptions.keySet())
        {
            subscriptionNumbers.put(subject, subscriptions.get(subject).size());
        }
        return subscriptionNumbers;
    }


    public HashMap<String, Integer> getDirectMessageSubscriptionNumbers()
    {
        HashMap<String, Integer> subscriptionNumbers = new HashMap<>();
        for (String MRN : wantsDirectMessages.keySet())
        {
            subscriptionNumbers.put(MRN, wantsDirectMessages.get(MRN).size());
        }
        return subscriptionNumbers;
    }
}
