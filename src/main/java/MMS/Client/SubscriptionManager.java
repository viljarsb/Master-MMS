package MMS.Client;

import MMS.AgentV2.Exceptions.SubscriptionException;
import MMS.MMTP.MessageFormats.*;
import com.google.protobuf.MessageLite;
import org.eclipse.jetty.websocket.api.Session;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

public class SubscriptionManager
{
    private final Set<String> subscriptions;


    public SubscriptionManager()
    {
        this.subscriptions = new ConcurrentSkipListSet<>();
    }


    public void subscribe(String subject, Session session) throws SubscriptionException
    {
        validateSubject(subject);
        subscriptions.add(subject);
        informRouter(MessageType.REGISTER, Register.newBuilder().addInterests(subject).build(), session);
    }


    public void subscribe(List<String> subjects, Session session) throws SubscriptionException
    {
        for (String subject : subjects)
        {
            validateSubject(subject);
        }
        subscriptions.addAll(subjects);
        informRouter(MessageType.REGISTER, Register.newBuilder().addAllInterests(subjects).build(), session);
    }


    public void unsubscribe(String subject, Session session)
    {
        if (subscriptions.remove(subject))
        {
            informRouter(MessageType.UNREGISTER, Unregister.newBuilder().addInterests(subject).build(), session);
        }
    }


    public void unsubscribe(List<String> subjects, Session session)
    {
        List<String> removed = new ArrayList<>();
        for (String subject : subjects)
        {
            if (subscriptions.remove(subject))
            {
                removed.add(subject);
            }
        }
        if (!removed.isEmpty())
        {
            informRouter(MessageType.UNREGISTER, Unregister.newBuilder().addAllInterests(removed).build(), session);
        }
    }


    public void unsubscribeAll(Session session)
    {
        if (!subscriptions.isEmpty())
        {
            informRouter(MessageType.UNREGISTER, Unregister.newBuilder().addAllInterests(subscriptions).build(), session);
            subscriptions.clear();
        }
    }



    public List<String> getSubscriptions()
    {
        return new ArrayList<>(subscriptions);
    }


    private void validateSubject(String subject) throws SubscriptionException
    {
        if (subject.length() < 1 || subject.length() > 100)
        {
            throw new SubscriptionException("Subject length must be between 1 and 100 characters.");
        }
    }


    private void informRouter(MessageType messageType, MessageLite messageContent, Session session)
    {
        ProtocolMessage message = ProtocolMessage.newBuilder()
                .setType(messageType)
                .setContent(messageContent.toByteString())
                .build();
        session.getRemote().sendBytesByFuture(ByteBuffer.wrap(message.toByteArray()));
    }
}