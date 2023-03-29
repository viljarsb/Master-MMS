package MMS.EdgeRouter;


import net.maritimeconnectivity.pki.PKIIdentity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private static final Logger logger = LogManager.getLogger(SubscriptionManager.class);
    private static final Comparator<Session> sessionComparator = Comparator.comparing(Session::hashCode);
    private static SubscriptionManager instance;

    private final ConcurrentHashMap<String, Set<Session>> subscriptions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<Session>> wantsDirectMessages = new ConcurrentHashMap<>();
    private final ConnectionHandler connectionHandler = ConnectionHandler.getHandler();


    /**
     * Private constructor for the SubscriptionManager class.
     * Initializes the concurrent maps and obtains an instance of the ConnectionHandler.
     */
    private SubscriptionManager()
    {
        logger.info("Subscription Manager Initialized");
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
            instance = new SubscriptionManager();

        return instance;
    }


    /**
     * Adds subscriptions for the specified subjects and the subscriber session.
     *
     * @param subjects   The list of subjects to subscribe to.
     * @param subscriber The subscriber session.
     */
    public void addSubscription(List<String> subjects, Session subscriber)
    {
        ThreadPoolService.executeAsync(() ->
        {
            AgentConnection connection = connectionHandler.getConnectionState(subscriber);
            for (String s : subjects)
            {
                if (s.length() > 100 || s.length() < 1)
                    continue;

                subscriptions.putIfAbsent(s, new ConcurrentSkipListSet<>(sessionComparator));
                boolean subscribed = subscriptions.get(s).add(subscriber);

                if (subscribed)
                    logger.info("Added subscription for subject {} for Agent ID: {}", s, connection.getAgentId());

                else
                    logger.info("Subscription already exists for subject {} for Agent ID: {}", s, connection.getAgentId());
            }
        }, TaskPriority.MEDIUM);
    }


    /**
     * Removes the subscription for the specified subjects and the subscriber session.
     *
     * @param subjects   The list of subjects to unsubscribe from.
     * @param subscriber The subscriber session.
     */
    public void removeSubscription(List<String> subjects, Session subscriber)
    {
        ThreadPoolService.executeAsync(() ->
        {
            AgentConnection connection = connectionHandler.getConnectionState(subscriber);
            for (String s : subjects)
            {
                boolean unsubscribed = subscriptions.get(s).remove(subscriber);

                if (unsubscribed)
                    logger.info("Subscription removed for subject '{}' by agent with ID '{}'", s, connection.getAgentId());

                else
                    logger.info("Subscription not found for subject '{}' by agent with ID '{}'", s, connection.getAgentId());
            }
        }, TaskPriority.MEDIUM);
    }


    /**
     * Adds a direct message subscription for the specified session.
     * Take note that a MRN can have multiple sessions, so we need to track the sessions.
     * If the session is not authenticated, it will be closed due to policy violation.
     *
     * @param session The session to add a direct message subscription for.
     */
    public void addDirectMessageSubscription(Session session)
    {
        ThreadPoolService.executeAsync(() ->
        {
            AgentConnection connection = connectionHandler.getConnectionState(session);
            PKIIdentity identity = connection.getIdentity();

            if (identity == null)
            {
                logger.error("An agent tried to subscribe to direct messages without authentication.");
                session.close(StatusCode.POLICY_VIOLATION, "Agent tried to subscribe to direct messages without authentication.");
            }

            else
            {
                String MRN = identity.getMrn();
                wantsDirectMessages.putIfAbsent(MRN, new ConcurrentSkipListSet<>(sessionComparator));
                boolean subscribed = wantsDirectMessages.get(MRN).add(session);

                if (subscribed)
                    logger.info("Added direct message subscription for MRN: {} for Agent ID: {}", MRN, connection.getAgentId());

                else
                    logger.info("Direct message subscription already exists for MRN: {} for Agent ID: {}", MRN, connection.getAgentId());
            }
        }, TaskPriority.MEDIUM);
    }


    /**
     * Removes the direct message subscription for the specified session.
     * Take note that a MRN can have multiple sessions, so we need to track the sessions.
     * If the session is not authenticated, it will be ignored.
     *
     * @param session The session to remove the direct message subscription from.
     */
    public void removeDirectMessageSubscription(Session session)
    {
        ThreadPoolService.executeAsync(() ->
        {
            AgentConnection connection = connectionHandler.getConnectionState(session);
            PKIIdentity identity = connection.getIdentity();

            if (!(identity == null))
            {
                String MRN = identity.getMrn();
                boolean unsubscribed = wantsDirectMessages.get(MRN).remove(session);

                if (unsubscribed)
                    logger.info("Direct message subscription removed for MRN: {} by agent with ID '{}'", MRN, connection.getAgentId());

                else
                    logger.info("Direct message subscription not found for MRN: {} by agent with ID '{}'", MRN, connection.getAgentId());
            }
        }, TaskPriority.MEDIUM);
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
            sessions.addAll(wantsDirectMessages.get(MRN));

        return sessions;
    }


    /**
     * Removes all subscriptions for the specified session.
     * This is called when a session is closed.
     *
     * @param session The session to remove all subscriptions for.
     */
    public void removeSession(Session session)
    {
        ThreadPoolService.executeAsync(() ->
        {
            for (String subject : subscriptions.keySet())
                subscriptions.get(subject).remove(session);

            for (String MRN : wantsDirectMessages.keySet())
                wantsDirectMessages.get(MRN).remove(session);
        }, TaskPriority.MEDIUM);
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


    public Set<Session> getMRNSubscribers(String MRN)
    {
        return new HashSet<>(wantsDirectMessages.get(MRN));
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


    /**
     * Returns a Map containing the number of subscriptions for each subject.
     *
     * @return A Map containing the number of subscriptions for each subject.
     */
    public Map<String, Integer> getSubscriptionNumbers()
    {
        HashMap<String, Integer> subscriptionNumbers = new HashMap<>();

        for (String subject : subscriptions.keySet())
        {
            subscriptionNumbers.put(subject, subscriptions.get(subject).size());
        }
        return subscriptionNumbers;
    }


    /**
     * Returns a Map containing the number of direct message subscriptions for each MRN.
     *
     * @return A Map containing the number of direct message subscriptions for each MRN.
     */
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
