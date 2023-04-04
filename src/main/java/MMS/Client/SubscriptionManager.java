package MMS.Client;


import MMS.Client.Connections.Connection;
import MMS.Client.Exceptions.InvalidSubjectException;
import MMS.Client.Exceptions.SendingException;
import MMS.Client.Interfaces.SubscribeListener;
import MMS.Protocols.MMTP.MessageFormats.MessageType;
import MMS.Protocols.MMTP.MessageFormats.ProtocolMessage;
import MMS.Protocols.MMTP.MessageFormats.Register;
import MMS.Protocols.MMTP.MessageFormats.Unregister;
import com.google.protobuf.ByteString;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages the subscription of subjects and direct messages for a connection.
 */
public class SubscriptionManager
{
    private final Set<String> subscriptions;
    private final Set<String> unconfirmedRemovals;
    private final AtomicBoolean wantsDirectMessages;
    private final SubscribeListener subscribeListener;


    /**
     * Creates a new SubscriptionManagement instance.
     *
     * @param subscribeListener The listener to receive subscription updates.
     */
    public SubscriptionManager(SubscribeListener subscribeListener)
    {
        this.subscribeListener = subscribeListener;
        this.subscriptions = new ConcurrentSkipListSet<>();
        this.unconfirmedRemovals = new ConcurrentSkipListSet<>();
        this.wantsDirectMessages = new AtomicBoolean(true);
    }


    /**
     * Subscribes the given subjects to the provided connection.
     *
     * @param subjects   The list of subjects to subscribe.
     * @param connection The connection to be subscribed.
     * @return The list of valid subjects.
     */
    public List<String> subscribe(List<String> subjects, Connection connection)
    {
        List<String> validSubjects = new ArrayList<>();
        List<String> invalidSubjects = new ArrayList<>();

        for (String subject : subjects)
        {
            try
            {
                validateSubject(subject);
                validSubjects.add(subject);
            }

            catch (InvalidSubjectException e)
            {
                invalidSubjects.add(subject);
            }
        }

        ByteString registerMessage = Register.newBuilder().addAllInterests(validSubjects).build().toByteString();
        MessageType messageType = MessageType.REGISTER;
        ProtocolMessage protocolMessage = ProtocolMessage.newBuilder().setType(messageType).setContent(registerMessage).build();

        if (invalidSubjects.size() > 0)
        {
            Throwable cause = new Throwable("Subjects must be between 1 and 100 characters");
            subscribeListener.onSubscriptionFailure(invalidSubjects, "Invalid subjects", cause);
        }


        try
        {
            connection.send(protocolMessage);
            subscriptions.addAll(validSubjects);
            subscribeListener.onSubscriptionSuccess(validSubjects);
            return validSubjects;
        }

        catch (SendingException ex)
        {
            subscribeListener.onSubscriptionFailure(validSubjects, "Failed to send subscription request", ex.getCause());
        }

        return validSubjects;
    }


    /**
     * Unsubscribes the given subjects from the provided connection.
     *
     * @param subjects   The list of subjects to unsubscribe.
     * @param connection The connection to be unsubscribed.
     * @return The list of subjects removed.
     */
    public List<String> unsubscribe(List<String> subjects, Connection connection)
    {
        List<String> subjectsToRemove = new ArrayList<>();

        for (String subject : subjects)
        {
            if (subscriptions.contains(subject))
                subjectsToRemove.add(subject);
        }

        ByteString unregisterMessage = Unregister.newBuilder().addAllInterests(subjectsToRemove).build().toByteString();
        MessageType messageType = MessageType.UNREGISTER;
        ProtocolMessage protocolMessage = ProtocolMessage.newBuilder().setType(messageType).setContent(unregisterMessage).build();

        try
        {
            connection.send(protocolMessage);
        }

        catch (SendingException ex)
        {
            unconfirmedRemovals.addAll(subjectsToRemove); // we could not tell router, but we can ignore it.
        }

        subjectsToRemove.forEach(subscriptions::remove);
        subscribeListener.onSubscriptionRemoved(subjectsToRemove);
        return subjectsToRemove;
    }


    /**
     * Subscribes the connection to direct messages.
     *
     * @param connection The connection to be subscribed.
     */
    public void subscribeToDM(Connection connection)
    {
        if (wantsDirectMessages.get())
        {
            subscribeListener.onDirectMessageSubscriptionChanged(true); // already subscribed, but we'll tell the listener anyway.
            return;
        }

        ByteString registerMessage = Register.newBuilder().setWantDirectMessages(true).build().toByteString();
        MessageType messageType = MessageType.REGISTER;
        ProtocolMessage protocolMessage = ProtocolMessage.newBuilder().setType(messageType).setContent(registerMessage).build();

        try
        {
            connection.send(protocolMessage);
            wantsDirectMessages.set(true);
            subscribeListener.onDirectMessageSubscriptionChanged(true);
        }

        catch (SendingException ex)
        {
            subscribeListener.onDirectMessageSubscriptionFailure("Failed to send subscription request", ex.getCause());
        }
    }


    /**
     * Unsubscribes the connection from direct messages.
     *
     * @param connection The connection to be unsubscribed.
     */
    public void unsubscribeFromDM(Connection connection)
    {
        if (!wantsDirectMessages.get())
        {
            subscribeListener.onDirectMessageSubscriptionChanged(false); // already unsubbed, but we'll tell the listener anyway.
            return;
        }

        ByteString unregisterMessage = Unregister.newBuilder().setWantDirectMessages(true).build().toByteString();
        MessageType messageType = MessageType.UNREGISTER;
        ProtocolMessage protocolMessage = ProtocolMessage.newBuilder().setType(messageType).setContent(unregisterMessage).build();

        try
        {
            connection.send(protocolMessage);
        }

        catch (SendingException ignored)
        {
        } // we could not tell router, but we can ignore it.

        subscribeListener.onDirectMessageSubscriptionChanged(false);
    }


    /**
     * Checks if the subject is currently subscribed.
     *
     * @param subject The subject to check.
     * @return true if the subject is subscribed, false otherwise.
     */
    public boolean isSubscribed(String subject)
    {
        return subscriptions.contains(subject);
    }


    /**
     * Checks if the connection is subscribed to direct messages.
     *
     * @return true if the connection is subscribed to direct messages, false otherwise.
     */
    public boolean wantsDirectMessages()
    {
        return wantsDirectMessages.get();
    }


    /**
     * Returns the list of current subscriptions.
     *
     * @return A list of the current subscriptions.
     */
    public List<String> getSubscriptions()
    {
        return new ArrayList<>(subscriptions);
    }


    /**
     * Validates the given subject.
     *
     * @param subject The subject to validate.
     * @throws InvalidSubjectException if the subject is invalid.
     */
    private void validateSubject(String subject) throws InvalidSubjectException
    {
        if (subject == null)
            throw new InvalidSubjectException("Subject cannot be null");

        if (subject.length() > 100 || subject.length() < 1)
            throw new InvalidSubjectException("Subject must be between 1 and 100 characters");
    }
}