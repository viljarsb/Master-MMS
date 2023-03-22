package MMS.Agent;

import java.time.Instant;

/**
 * The AgentCallback interface provides methods for processing directed and subject cast
 * application messages. To handle these message types, implement this interface and register
 * the resulting object with the Agent during instantiation. The implemented methods will be
 * invoked when the corresponding message types are received.
 */
public interface AgentCallback
{
    /**
     * This method is invoked when an application message is received that is subject cast to the agent.
     * @param subject The subject of the message
     * @param message The message payload
     * @param expires The expiration time of the message
     * @param sender The MRN of the sender
     * @param id The message ID
     */
    void onSubjectCast(String subject, byte[] message, Instant expires, String sender, String id);
}
