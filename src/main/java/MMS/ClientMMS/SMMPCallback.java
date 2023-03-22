package MMS.ClientMMS;


/**
 * Callback interface for the SMMP client
 */
public interface SMMPCallback
{
    /**
     * Called when a direct message is received
     *
     * @param message The message
     * @param sender  The sender's MRN
     */
    void onDirectMessage(byte[] message, String sender);


    /**
     * Called when a subject cast message is received
     *
     * @param message The message
     * @param subject The subject
     */
    void onSubjectCast(byte[] message, String subject);


    /**
     * Called when a message is delivered
     *
     * @param id  The message ID
     * @param mrn The MRN of the recipient
     */
    void onMessageDelivered(String id, String mrn);


    /**
     * Called when a message delivery fails
     *
     * @param id  The message ID
     * @param mrn The MRN of the recipient
     */
    void onMessageDeliveryFailed(String id, String mrn);
}
