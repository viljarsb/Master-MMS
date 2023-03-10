package MMS.MMTP;

import java.lang.reflect.Array;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class ApplicationMessage extends MMTPMessage
{
    private final UUID messageId;
    private final URI sender;
    private final URI topic;
    private final ArrayList<URI> recipients;
    private final long expiryTime;
    private final Byte[] payload;


    private ApplicationMessage(Builder builder)
    {
        Objects.requireNonNull(builder.sender, "Sender must not be null");
        Objects.requireNonNull(builder.payload, "Payload must not be null");
        if (builder.topic == null && builder.recipients.isEmpty())
        {
            throw new IllegalStateException("Either topic or recipients must be set");
        }
        if (builder.expiryTime != null)
        {
            long now = Instant.now().toEpochMilli();
            if (builder.expiryTime < now)
            {
                throw new IllegalArgumentException("Expiry time cannot be in the past");
            }
            if (builder.expiryTime > now + 30L * 24L * 60L * 60L * 1000L)
            {
                throw new IllegalArgumentException("Expiry time cannot be more than 30 days in the future");
            }
        }
        this.messageId = UUID.randomUUID();
        this.sender = builder.sender;
        this.topic = builder.topic;
        this.recipients = builder.recipients;
        this.expiryTime = builder.expiryTime != null ? builder.expiryTime : Instant.now().toEpochMilli();
        this.payload = builder.payload;
    }


    public UUID getMessageId()
    {
        return messageId;
    }


    public URI getSender()
    {
        return sender;
    }


    public URI getTopic()
    {
        return topic;
    }


    public ArrayList<URI> getRecipients()
    {
        return recipients;
    }


    public long getExpiryTime()
    {
        return expiryTime;
    }


    public Byte[] getPayload()
    {
        return payload;
    }


    public static class Builder
    {
        private URI sender;
        private URI topic = null;
        private ArrayList<URI> recipients = new ArrayList<>();
        private Long expiryTime = null;
        private Byte[] payload = null;


        public Builder sender(URI sender)
        {
            this.sender = sender;
            return this;
        }


        public Builder topic(URI topic)
        {
            this.topic = topic;
            return this;
        }


        public Builder recipient(URI recipient)
        {
            this.recipients.add(recipient);
            return this;
        }


        public Builder expiryTime(long expiryTime)
        {
            this.expiryTime = expiryTime;
            return this;
        }


        public Builder payload(Byte[] payload)
        {
            this.payload = payload;
            return this;
        }


        public ApplicationMessage build()
        {
            return new ApplicationMessage(this);
        }
    }
}