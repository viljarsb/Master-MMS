package MMS.AgentV2;

public class MessageHandler
{
    private final AgentAdapter adapter;

    public MessageHandler(AgentAdapter adapter)
    {
        this.adapter = adapter;
    }

    public void processMessage(byte[] payload, int offset, int len)
    {
        byte[] message = new byte[len];
        System.arraycopy(payload, offset, message, 0, len);
        Protoco

        try
        {

        }
    }
}
