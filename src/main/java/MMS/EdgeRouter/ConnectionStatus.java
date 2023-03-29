package MMS.EdgeRouter;

import org.jetbrains.annotations.NotNull;

public class ConnectionStatus
{
    private final double expectedBandwidth;
    private final double expectedDelay;

    public ConnectionStatus(double expectedBandwidth, double expectedDelay)
    {
        this.expectedBandwidth = expectedBandwidth;
        this.expectedDelay = expectedDelay;
    }

    public double getExpectedBandwidth()
    {
        return expectedBandwidth;
    }

    public double getExpectedDelay()
    {
        return expectedDelay;
    }
}
