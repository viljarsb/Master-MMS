package MMS.ClientMMS.Handlers;

import MMS.Agent.Agent;
import MMS.Agent.Exceptions.AnonymousConnectionException;
import MMS.Agent.Exceptions.MessageSendingException;
import MMS.Agent.Exceptions.NotConnectedException;
import MMS.ClientMMS.Crypto.CryptoEngine;
import MMS.ClientMMS.SMMPCallback;
import MMS.SMMPMessages.SMMPMessage;
import MMS.SMMPMessages.SMMPMessageAck;
import com.google.protobuf.ByteString;
import net.maritimeconnectivity.pki.PKIIdentity;

import java.security.interfaces.ECPrivateKey;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AckHandler
{
    private SMMPCallback smmpCallback;
    private KeyStoreHandler keyStoreHandler;
    private final ConcurrentHashMap<String, PendingAck> waitingForAck;
    private final ConcurrentHashMap<String, Instant> subjectCastAcks;
    private final ExecutorService workerPool;
    private Agent agent;


    public AckHandler()
    {
        this.waitingForAck = new ConcurrentHashMap<>();
        this.subjectCastAcks = new ConcurrentHashMap<>();
        this.workerPool = Executors.newFixedThreadPool(10);
    }


    public void setCallback(SMMPCallback smmpCallback)
    {
        this.smmpCallback = smmpCallback;
    }


    public void setKeyStoreHandler(KeyStoreHandler keyStoreHandler)
    {
        this.keyStoreHandler = keyStoreHandler;
    }


    public void setAgent(Agent agent)
    {
        this.agent = agent;
    }


    public void handleAck(String messageID, String senderOfAck)
    {
        if (waitingForAck.containsKey(messageID))
        {
            waitingForAck.remove(messageID);
            smmpCallback.onMessageDelivered(messageID, senderOfAck);
        }
        else if (subjectCastAcks.containsKey(messageID))
        {
            subjectCastAcks.remove(messageID);
            smmpCallback.onMessageDelivered(messageID, senderOfAck);
        }
    }


    public void requireDirectMessageAck(SMMPMessage message, String destination, Instant expires, String messageId)
    {
        PendingAck pendingAck = new PendingAck(message, destination, expires);
        waitingForAck.put(messageId, pendingAck);
        workerPool.execute(() -> exponentialBackoff(messageId));
    }


    public void monitorSubjectCastAcks(Instant expires, String messageId)
    {
        subjectCastAcks.put(messageId, expires);
    }


    private void exponentialBackoff(String messageId)
    {
        int attempts = 0;
        long backoffTime = 5000; // 5 seconds initial backoff time
        long maxBackoffTime = 600000; // 10 minutes maximum backoff time

        while (waitingForAck.containsKey(messageId) && backoffTime <= maxBackoffTime)
        {
            try
            {
                TimeUnit.MILLISECONDS.sleep(backoffTime);

                PendingAck pendingAck = waitingForAck.get(messageId);
                SMMPMessage message = pendingAck.getMessage();
                String destination = pendingAck.getDestination();
                Instant expires = pendingAck.getExpires();

                if (expires.isBefore(Instant.now()))
                {
                    waitingForAck.remove(messageId);
                    return;
                }

                String newMessageID = agent.sendDirectMessage(destination, message.toByteArray(), expires);
                waitingForAck.remove(messageId);
                waitingForAck.put(newMessageID, pendingAck);

                attempts++;
                backoffTime = (long) (backoffTime * Math.pow(2, attempts));
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            catch (AnonymousConnectionException | NotConnectedException | MessageSendingException e)
            {
                throw new RuntimeException(e);
            }
        }

        if (waitingForAck.containsKey(messageId))
        {
            PendingAck pendingAck = waitingForAck.remove(messageId);
            smmpCallback.onMessageDeliveryFailed(messageId, pendingAck.getDestination());
        }
    }


    public void sendAck(String messageID, String MRN)
    {
        try
        {
            ECPrivateKey privateKey = keyStoreHandler.getMyPrivateKey();
            byte[] signature = CryptoEngine.sign(messageID.getBytes(), privateKey);
            PKIIdentity pkiIdentity = keyStoreHandler.getMyPkiIdentity();
            String X5U = pkiIdentity.getUrl();

            SMMPMessageAck ackMessage = SMMPMessageAck.newBuilder()
                    .setAck(messageID)
                    .setSignature(ByteString.copyFrom(signature))
                    .setX5U(X5U)
                    .build();

            this.agent.sendDirectMessage(MRN, ackMessage.toByteArray(), null);
        }
        catch (Exception e)
        {
            System.out.println("Failed to send ack");
        }
    }


    private static class PendingAck
    {
        private final SMMPMessage message;
        private final String destination;
        private final Instant expires;


        public PendingAck(SMMPMessage message, String destination, Instant expires)
        {
            this.message = message;
            this.destination = destination;
            this.expires = expires;
        }


        public SMMPMessage getMessage()
        {
            return message;
        }


        public String getDestination()
        {
            return destination;
        }


        public Instant getExpires()
        {
            return expires;
        }
    }
}
