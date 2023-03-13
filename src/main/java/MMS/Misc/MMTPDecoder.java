package MMS.Misc;

import MMS.MMTP.MMTPMessage;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class MMTPDecoder implements Decoder.Text<MMTPMessage>
{
    private ObjectMapper objectMapper;


    @Override
    public MMTPMessage decode(String s) throws DecodeException
    {
        try
        {
            JsonNode rootNode = objectMapper.readTree(s);
            Iterator<String> fieldNames = rootNode.fieldNames();
            if (!fieldNames.hasNext())
            {
                throw new DecodeException(s, "Unable to decode message");
            }
            String key = fieldNames.next();
            if (fieldNames.hasNext())
            {
                throw new DecodeException(s, "Unexpected number of fields in message");
            }
            if (!rootNode.get(key).isTextual())
            {
                throw new DecodeException(s, "Invalid message structure");
            }
            String json = rootNode.get(key).asText();
            Class<? extends MMTPMessage> clazz = (Class<? extends MMTPMessage>) Class.forName("MMTP." + key.toUpperCase());
            return objectMapper.readValue(json, clazz);
        }
        catch (IOException | ClassNotFoundException e)
        {
            throw new DecodeException(s, "Unable to decode message", e);
        }
    }


    @Override
    public boolean willDecode(String s)
    {
        try
        {
            JsonNode rootNode = objectMapper.readTree(s);
            Iterator<String> fieldNames = rootNode.fieldNames();
            if (!fieldNames.hasNext())
            {
                return false;
            }
            String key = fieldNames.next();
            if (fieldNames.hasNext())
            {
                return false;
            }
            if (!rootNode.get(key).isTextual())
            {
                return false;
            }
            Class<? extends MMTPMessage> clazz = (Class<? extends MMTPMessage>) Class.forName("MMTP." + key.toUpperCase());
            return true;
        }
        catch (IOException | ClassNotFoundException e)
        {
            return false;
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

