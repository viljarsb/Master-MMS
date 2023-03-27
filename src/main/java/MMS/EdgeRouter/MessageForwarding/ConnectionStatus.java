package MMS.EdgeRouter.MessageForwarding;

public class ConnectionStatus
{
    private final int expectedBandwidth;
    private final int expectedDelay;

    public ConnectionStatus(int expectedBandwidth, int expectedDelay)
    {
        this.expectedBandwidth = expectedBandwidth;
        this.expectedDelay = expectedDelay;
    }

    public int getExpectedBandwidth()
    {
        return expectedBandwidth;
    }

    public int getExpectedDelay()
    {
        return expectedDelay;
    }
}
