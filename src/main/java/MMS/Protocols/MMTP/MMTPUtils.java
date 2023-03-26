package MMS.Protocols.MMTP;

import MMS.Protocols.MMTP.MessageFormats.DirectApplicationMessage;
import MMS.Protocols.MMTP.MessageFormats.SubjectCastApplicationMessage;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class MMTPUtils
{
    /**
     * This method is used to create a SubjectCastApplicationMessage object.
     *
     * @param subject The subject to send the message to.
     * @param sender The sender of the message.
     * @param payload The payload of the message.
     * @param expires The expiration time of the message.
     * @return SubjectCastApplicationMessage The created SubjectCastApplicationMessage object.
     */
    public static SubjectCastApplicationMessage createSubjectCastApplicationMessage(String subject, String sender, byte[] payload, Instant expires)
    {
        SubjectCastApplicationMessage.Builder builder = SubjectCastApplicationMessage.newBuilder();
        String messageId = UUID.randomUUID().toString();

        builder.setId(messageId)
                .setSubject(subject)
                .setSender(sender);

        if (expires != null)
        {
            builder.setExpires(Timestamp.newBuilder()
                    .setSeconds(expires.getEpochSecond())
                    .setNanos(expires.getNano())
                    .build());
        }

        builder.setPayload(ByteString.copyFrom(payload));
        return builder.build();
    }


    /**
     * This method is used to create a DirectApplicationMessage object.
     *
     * @param destinations The list of destinations to send the message to.
     * @param sender       The sender of the message.
     * @param payload      The payload of the message.
     * @param expires      The expiration time of the message.
     * @return DirectApplicationMessage The created DirectApplicationMessage object.
     */
    public static DirectApplicationMessage createDirectApplicationMessage(List<String> destinations, String sender, byte[] payload, Instant expires)
    {
        DirectApplicationMessage.Builder builder = DirectApplicationMessage.newBuilder();
        String messageId = UUID.randomUUID().toString();

        builder.setId(messageId)
                .addAllRecipients(destinations)
                .setSender(sender);

        if (expires != null)
        {
            builder.setExpires(Timestamp.newBuilder()
                    .setSeconds(expires.getEpochSecond())
                    .setNanos(expires.getNano())
                    .build());
        }

        builder.setPayload(ByteString.copyFrom(payload));
        return builder.build();
    }


}
