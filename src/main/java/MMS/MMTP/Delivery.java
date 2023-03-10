package MMS.MMTP;

import java.util.ArrayList;
import java.util.Objects;

public class Delivery extends MMTPMessage
{
    private final ArrayList<ApplicationMessage> messages;


    private Delivery(Builder builder)
    {
        Objects.requireNonNull(builder.messages, "Messages must not be null");
        if (builder.messages.isEmpty())
        {
            throw new IllegalArgumentException("At least one message must be specified");
        }
        this.messages = builder.messages;
    }


    public ArrayList<ApplicationMessage> getMessages()
    {
        return this.messages;
    }


    public static class Builder
    {
        private final ArrayList<ApplicationMessage> messages = new ArrayList<>();


        public Builder addMessage(ApplicationMessage message)
        {
            messages.add(message);
            return this;
        }


        public Delivery build()
        {
            return new Delivery(this);
        }
    }
}

