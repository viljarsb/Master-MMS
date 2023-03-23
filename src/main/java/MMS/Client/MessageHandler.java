package MMS.Client;

import MMS.MMTP.MessageFormats.DirectApplicationMessage;
import MMS.MMTP.MessageFormats.MessageType;
import MMS.MMTP.MessageFormats.ProtocolMessage;
import MMS.MMTP.MessageFormats.SubjectCastApplicationMessage;
import MMS.MMTP.Validators.MMTPValidator;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageHandler
{
    private final static Logger logger = LogManager.getLogger(MessageHandler.class);

    private final AgentAdapter adapter;
    private final ExecutorService workerPool;


    public MessageHandler(AgentAdapter adapter)
    {
        this.adapter = adapter;
        int cores = Runtime.getRuntime().availableProcessors();
        workerPool = Executors.newFixedThreadPool(cores);
    }


    public void processMessage(byte[] payload, int offset, int len)
    {
        delegate(() -> processMessageInternal(payload, offset, len));
    }


    private void processMessageInternal(byte[] payload, int offset, int len)
    {
        byte[] message = new byte[len];
        System.arraycopy(payload, offset, message, 0, len);

        try
        {
            ProtocolMessage protocolMessage = ProtocolMessage.parseFrom(message);
            MessageType type = protocolMessage.getType();

            switch (type)
            {
                case DIRECT_APPLICATION_MESSAGE ->
                {
                    DirectApplicationMessage directApplicationMessage = DirectApplicationMessage.parseFrom(protocolMessage.getContent());
                    processMessage(directApplicationMessage);
                }

                case SUBJECT_CAST_APPLICATION_MESSAGE ->
                {
                    SubjectCastApplicationMessage subjectCastApplicationMessage = SubjectCastApplicationMessage.parseFrom(protocolMessage.getContent());
                    processMessage(subjectCastApplicationMessage);
                }

                default ->
                {
                    logger.error("Unknown message type: " + type);
                }
            }
        }

        catch (InvalidProtocolBufferException ex)
        {
            logger.error("Invalid protocol buffer", ex);
        }
    }


    private void processMessage(DirectApplicationMessage message)
    {
        if (!MMTPValidator.validate(message))
        {
            logger.error("The application message is not valid, dropping it");
            return;
        }

        String messageId = message.getId();
        String sender = message.getSender();
        Instant expires = Instant.ofEpochSecond(message.getExpires().getSeconds(), message.getExpires().getNanos());
        byte[] content = message.getPayload().toByteArray();

        adapter.onDirectMessage(messageId, sender, expires, content);
    }


    private void processMessage(SubjectCastApplicationMessage message)
    {
        if (!MMTPValidator.validate(message))
        {
            logger.error("The application message is not valid, dropping it");
            return;
        }

        String messageId = message.getId();
        String sender = message.getSender();
        Instant expires = Instant.ofEpochSecond(message.getExpires().getSeconds(), message.getExpires().getNanos());
        String subject = message.getSubject();
        byte[] content = message.getPayload().toByteArray();

        adapter.onSubjectCastMessage(messageId, sender, subject, expires, content);
    }

    private void delegate(Runnable runnable)
    {
        workerPool.execute(runnable);
    }
}
