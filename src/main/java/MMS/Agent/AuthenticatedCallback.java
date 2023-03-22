package MMS.Agent;

import java.time.Instant;

public interface AuthenticatedCallback extends AgentCallback
{
    /**
     * This method is invoked when an application message is received that is directed to the agent.
     * @param message The message payload
     * @param expires The expiration time of the message
     * @param sender The MRN of the sender
     * @param id The message ID
     */
    void onDirectMessage(byte[] message, Instant expires, String sender, String id);
}
