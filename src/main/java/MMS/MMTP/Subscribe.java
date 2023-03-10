package MMS.MMTP;

import java.net.URI;
import java.util.ArrayList;
import java.util.Objects;

public class Subscribe extends MMTPMessage
{
    private final ArrayList<URI> topics;


    private Subscribe(Builder builder)
    {
        Objects.requireNonNull(builder.topics, "Topics must not be null");
        if (builder.topics.isEmpty())
        {
            throw new IllegalArgumentException("At least one topic must be specified");
        }
        this.topics = builder.topics;
    }


    public ArrayList<URI> getTopics()
    {
        return this.topics;
    }


    public static class Builder
    {
        private final ArrayList<URI> topics = new ArrayList<>();


        public Builder addTopic(String topic)
        {
            URI urn = URI.create(topic);
            topics.add(urn);
            return this;
        }


        public Builder addTopics(ArrayList<String> topics)
        {
            for (String topic : topics)
            {
                URI urn = URI.create(topic);
                this.topics.add(urn);
            }
            return this;
        }


        public Subscribe build()
        {
            return new Subscribe(this);
        }
    }
}
