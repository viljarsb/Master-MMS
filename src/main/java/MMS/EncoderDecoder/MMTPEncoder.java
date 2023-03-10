package MMS.EncoderDecoder;

import MMS.MMTP.MMTPMessage;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import java.nio.ByteBuffer;

public class MMTPEncoder implements Encoder.Text<MMTPMessage>
{
    private ObjectMapper objectMapper;

    @Override
    public String encode(MMTPMessage message) throws EncodeException
    {
        ObjectNode objectNode = objectMapper.createObjectNode();

        try
        {
            String json = objectMapper.writeValueAsString(message);
            String key = message.getClass().getSimpleName().toLowerCase();
            objectNode.put(key, json);
        }

        catch (JsonProcessingException e)
        {
            throw new EncodeException(message, "Unable to encode message", e);
        }

        try
        {
            return objectMapper.writeValueAsString(objectNode);
        }

        catch (JsonProcessingException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void init(EndpointConfig config)
    {
        this.objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public void destroy()
    {
    }

}
