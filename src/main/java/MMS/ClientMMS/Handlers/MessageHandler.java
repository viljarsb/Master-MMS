package MMS.ClientMMS.Handlers;

import MMS.Agent.AgentCallback;
import MMS.ClientMMS.Crypto.CryptoEngine;
import MMS.ClientMMS.Exceptions.MessageProcessingException;
import MMS.ClientMMS.Exceptions.RemotePublicKeyException;
import MMS.ClientMMS.Exceptions.UnknownTypeException;
import MMS.ClientMMS.Exceptions.X509UrlFetchException;
import MMS.ClientMMS.SMMPCallback;
import MMS.SMMPMessages.SMMPMessage;
import MMS.SMMPMessages.SMMPMessageAck;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.KeyStoreException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageHandler implements AgentCallback
{

    private static final Logger logger = LogManager.getLogger(MessageHandler.class);

    private final ExecutorService workerPool;
    private KeyStoreHandler keyStoreHandler;
    private SMMPCallback smmpCallback;
    private AckHandler ackHandler;
    private final Map<String, Boolean> alreadyDelivered = new ConcurrentHashMap<>();


    public MessageHandler()
    {
        this.workerPool = Executors.newFixedThreadPool(10);
        logger.info("Initialized Agent Message Callback Handler");
    }

    public void setKeyStoreHandler (KeyStoreHandler keyStoreHandler)
    {
        this.keyStoreHandler = keyStoreHandler;
    }

    public void setCallback (SMMPCallback smmpCallback)
    {
        this.smmpCallback = smmpCallback;
    }

    public void setAckHandler (AckHandler ackHandler)
    {
        this.ackHandler = ackHandler;
    }




    @Override
    public void onDirectMessage(byte[] message, Instant expires, String sender, String id)
    {
        logger.info("Received direct message from agent, processing...");
        workerPool.execute(() ->
        {
            try
            {
                processMessage(message, sender, id, MessageType.DIRECT_MESSAGE);
            }
            catch (UnknownTypeException | MessageProcessingException ex)
            {
                logger.error("Error processing message", ex);
            }
        });
    }


    @Override
    public void onSubjectCast(String subject, byte[] message, Instant expires, String sender, String id)
    {
        logger.info("Received subject cast from agent, processing...");
        workerPool.execute(() ->
        {
            try
            {
                processMessage(message, sender, id, MessageType.SUBJECT_CAST);
            }
            catch (UnknownTypeException | MessageProcessingException ex)
            {
                logger.error("Error processing message", ex);
            }
        });
    }


    private void processMessage(byte[] payload, String sender, String id, MessageType type) throws UnknownTypeException, MessageProcessingException
    {

        Message message = determineMessageType(payload);

        if (message instanceof SMMPMessage)
        {
            processSMMPMessage((SMMPMessage) message, sender, id, type);
        }
        else if (message instanceof SMMPMessageAck)
        {
            processSMMPAck((SMMPMessageAck) message, sender);
        }
    }


    private Message determineMessageType(byte[] payload) throws UnknownTypeException
    {
        try
        {
            return SMMPMessage.parseFrom(payload);
        }

        catch (InvalidProtocolBufferException ignored)
        {
        }

        try
        {
            return SMMPMessageAck.parseFrom(payload);
        }

        catch (InvalidProtocolBufferException ignored)
        {
        }

        throw new UnknownTypeException("Unknown message type");
    }


    private void processSMMPMessage(SMMPMessage message, String sender, String id, MessageType type) throws MessageProcessingException
    {
        try
        {
            byte[] payload = message.getPayload().toByteArray();
            byte[] signature = message.getSignature().toByteArray();

            ECPublicKey publicKey = getPublicKey(sender, message.getX5U());
            if (publicKey == null)
            {
                throw new MessageProcessingException("Cannot get the public key of the sender");
            }

            if (message.getIsEncrypted())
            {
                payload = decryptMessage(payload, publicKey);
            }

            if (!CryptoEngine.verifySignature(publicKey, payload, signature))
            {
                throw new MessageProcessingException("Signature is not valid");
            }

            if (alreadyDelivered.putIfAbsent(id, true) == null)
            {
                if (type == MessageType.DIRECT_MESSAGE)
                {
                    smmpCallback.onDirectMessage(payload, sender);
                }
                else if (type == MessageType.SUBJECT_CAST)
                {
                    smmpCallback.onSubjectCast(payload, sender);
                }
                ackHandler.sendAck(id, sender);
            }
        }

        catch (Exception ex)
        {
            throw new MessageProcessingException("Error processing message", ex);
        }
    }


    public byte[] decryptMessage(byte[] payload, ECPublicKey publicKey) throws MessageProcessingException
    {
        try
        {
            ECPrivateKey privateKey = keyStoreHandler.getMyPrivateKey();
            return CryptoEngine.decryptMessage(privateKey, publicKey, payload);
        }
        catch (Exception ex)
        {
            throw new MessageProcessingException("Error decrypting message", ex);
        }
    }


    private void processSMMPAck(SMMPMessageAck message, String sender) throws MessageProcessingException
    {
        try
        {
            String ackId = message.getAck();
            byte[] signature = message.getSignature().toByteArray();
            ECPublicKey publicKey = getPublicKey(sender, message.getX5U());
            if (publicKey == null)
            {
                throw new MessageProcessingException("Cannot get the public key of the sender");
            }

            if (!CryptoEngine.verifySignature(publicKey, ackId.getBytes(), signature))
            {
                throw new MessageProcessingException("Signature is not valid");
            }

            ackHandler.handleAck(ackId, sender);
        }

        catch (Exception ex)
        {
            throw new MessageProcessingException("Error processing message", ex);
        }
    }


    private ECPublicKey getPublicKey(String sender, String x5u) throws RemotePublicKeyException, KeyStoreException, X509UrlFetchException
    {
        ECPublicKey publicKey = keyStoreHandler.getRemotePublicKey(sender);
        if (publicKey == null)
        {
            publicKey = keyStoreHandler.getECPublicKeyFromX509URL(x5u);
        }
        return publicKey;
    }


    private enum MessageType
    {
        DIRECT_MESSAGE,
        SUBJECT_CAST
    }
}
