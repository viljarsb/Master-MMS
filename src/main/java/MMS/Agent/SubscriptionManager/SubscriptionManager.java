package MMS.Agent.SubscriptionManager;

import MMS.Misc.MrnValidator;
import com.google.protobuf.Extension;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;

public class SubscriptionManager
{
    private static final Logger logger = LogManager.getLogger(SubscriptionManager.class);

    private final ArrayList<String> subscriptions;


    public SubscriptionManager()
    {
        subscriptions = new ArrayList<>();
    }


    public void addSubscription(String subject)
    {
        if (subject.length() > 100 || subject.length() < 1)
            throw new IllegalArgumentException("Subscription " + subject + " is invalid");

        if (subscriptions.contains(subject))
            throw new IllegalArgumentException("Subscription " + subject + " already exists");

        subscriptions.add(subject);
    }


    public void addSubscriptions(List<String> subjects) throws IllegalArgumentException
    {
        for (String subject : subjects)
        {
            if (subject.length() > 100 || subject.length() < 1)
                throw new IllegalArgumentException("Subscription " + subject + " is invalid");

            if (subscriptions.contains(subject))
                throw new IllegalArgumentException("Subscription " + subject + " already exists");
        }

        subscriptions.addAll(subjects);
    }


    public void removeSubscription(String subject)
    {
        if (subject.length() > 100 || subject.length() < 1)
            throw new IllegalArgumentException("Subscription " + subject + " is invalid");

        if (!subscriptions.contains(subject))
            throw new IllegalArgumentException("Subscription " + subject + " does not exist");

        subscriptions.remove(subject);
    }


    public void removeSubscriptions(List<String> subjects)
    {
        for (String subject : subjects)
        {
            if (subject.length() > 100 || subject.length() < 1)
                throw new IllegalArgumentException("Subscription " + subject + " is invalid");

            if (!subscriptions.contains(subject))
                throw new IllegalArgumentException("Subscription " + subject + " does not exist");
        }

        subscriptions.removeAll(subjects);
    }


    public List<String> getSubscriptions()
    {
        return new ArrayList<>(subscriptions);
    }


    public void clearSubscriptions()
    {
        subscriptions.clear();
    }
}
