package MMS.Client.Interfaces;

import java.time.Instant;
import java.util.List;


/**
 * Interface used to inform the Agent of messages received from the Edge Router.
 */
public interface MessageListener
{
    void onDirectMessage(String messageId, List<String> destinations, String sender, Instant expires, byte[] message);
    void onSubjectCastMessage(String messageId, String sender, String subject, Instant expires, byte[] message);
}
