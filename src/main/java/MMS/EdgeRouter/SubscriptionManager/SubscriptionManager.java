package MMS.EdgeRouter.SubscriptionManager;


import MMS.EdgeRouter.WebsocketServerManager.ConnectionHandler;
import MMS.EdgeRouter.WebsocketServerManager.ConnectionState;
import net.maritimeconnectivity.pki.PKIIdentity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SubscriptionManager
{
    private static final Logger logger = LogManager.getLogger(SubscriptionManager.class);
    private static SubscriptionManager instance;

    private final ConcurrentHashMap<String, Set<String>> subscriptions;
    private final ConcurrentHashMap<String, Boolean> dmSubscriptions;


    public SubscriptionManager()
    {
        subscriptions = new ConcurrentHashMap<>();
        dmSubscriptions = new ConcurrentHashMap<>();
    }


    public static SubscriptionManager getManager()
    {
        if (instance == null)
        {
            instance = new SubscriptionManager();
        }
        return instance;
    }


    public void subscribe(List<String> subjects, String connectionID)
    {
        logger.info("Processing subscribe message");
        for (String subject : subjects)
        {
            addSubscription(subject, connectionID);
        }
    }


    public void unsubscribe(List<String> subjects, String connectionID)
    {
        logger.info("Processing subscribe message");
        for (String subject : subjects)
        {
            removeSubscription(subject, connectionID);
        }
    }


    private void addSubscription(String subject, String connectionID)
    {
        if (subject.length() > 100 || subject.length() < 1)
        {
            logger.error("Invalid topic: " + subject);
            return;
        }

        if (!subscriptions.containsKey(subject))
        {
            addNewSubject(subject);
        }

        subscriptions.get(subject).add(connectionID);
    }


    private void removeSubscription(String subject, String connectionID)
    {
        if (!subscriptions.containsKey(subject))
        {
            logger.error("No such subscription: " + subject);
            return;
        }

        subscriptions.get(subject).remove(connectionID);

        if (subscriptions.get(subject).isEmpty())
        {
            removeSubject(subject);
        }
    }


    private void addNewSubject(String subject)
    {
        subscriptions.put(subject, Collections.synchronizedSet(new HashSet<>()));
        //inform router.
    }


    private void removeSubject(String subject)
    {
        subscriptions.remove(subject);
        //inform router
    }


    public void subscribeToDM(String connectionID)
    {
        logger.info("Subscribing to DM");
        dmSubscriptions.put(connectionID, true);
        //inform router
    }


    public void unsubscribeFromDM(String connectionID)
    {
        logger.info("Unsubscribing from DM");
        dmSubscriptions.put(connectionID, false);
        //inform router
    }


    public void removeClient(String connectionID)
    {
        ConnectionState connectionState = ConnectionHandler.getHandler().getConnectionState(connectionID);

        for (String subject : subscriptions.keySet())
        {
            if (subscriptions.get(subject).contains(connectionID))
                removeSubscription(subject, connectionID);
        }

        unsubscribeFromDM(connectionID);
        logger.info("Client removed: " + connectionID);
    }


    public void addClient(String connectionID)
    {
        ConnectionState connectionState = ConnectionHandler.getHandler().getConnectionState(connectionID);
        PKIIdentity identity = connectionState.getIdentity();

        if (identity != null)
        {
            subscribeToDM(connectionID);
        }

        logger.info("Client added: " + connectionID);
    }
}
