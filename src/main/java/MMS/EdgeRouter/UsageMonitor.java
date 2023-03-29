package MMS.EdgeRouter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

public class UsageMonitor
{
    private static final UsageMonitor instance = new UsageMonitor();

    private final ConcurrentHashMap<String, LongAdder> bytesReceived = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LongAdder> bytesSent = new ConcurrentHashMap<>();
    private final LongAdder totalBytesReceived = new LongAdder();
    private final LongAdder totalBytesSent = new LongAdder();
    private final AtomicInteger totalConnections = new AtomicInteger();
    private final AtomicInteger totalMessagesReceived = new AtomicInteger();
    private final AtomicInteger totalMessagesSent = new AtomicInteger();


    private UsageMonitor() {}


    public static UsageMonitor getMonitor()
    {
        return instance;
    }


    public void addBytesReceived(String agentId, long bytes)
    {
        ThreadPoolService.executeAsync(() -> bytesReceived.computeIfAbsent(agentId, k -> new LongAdder()).add(bytes), TaskPriority.LOW);
        ThreadPoolService.executeAsync(() -> totalBytesReceived.add(bytes), TaskPriority.LOW);
        ThreadPoolService.executeAsync(totalMessagesReceived::incrementAndGet, TaskPriority.LOW);
    }


    public long getBytesReceived(String agentId)
    {
        return bytesReceived.getOrDefault(agentId, new LongAdder()).longValue();
    }


    public void addBytesSent(String agentId, long bytes)
    {
        ThreadPoolService.executeAsync(() -> bytesSent.computeIfAbsent(agentId, k -> new LongAdder()).add(bytes), TaskPriority.LOW);
        ThreadPoolService.executeAsync(() -> totalBytesSent.add(bytes), TaskPriority.LOW);
        ThreadPoolService.executeAsync(totalMessagesSent::incrementAndGet, TaskPriority.LOW);
    }


    public long getBytesSent(String agentId)
    {
        return bytesSent.getOrDefault(agentId, new LongAdder()).longValue();
    }


    public void addConnection(String agentID)
    {
        ThreadPoolService.executeAsync(() -> bytesReceived.computeIfAbsent(agentID, k -> new LongAdder()), TaskPriority.LOW);
        ThreadPoolService.executeAsync(() -> bytesSent.computeIfAbsent(agentID, k -> new LongAdder()), TaskPriority.LOW);
        ThreadPoolService.executeAsync(totalConnections::incrementAndGet, TaskPriority.LOW);
    }


    public void removeConnection(String agentID)
    {
        ThreadPoolService.executeAsync(() -> bytesReceived.remove(agentID), TaskPriority.LOW);
        ThreadPoolService.executeAsync(() -> bytesSent.remove(agentID), TaskPriority.LOW);
        ThreadPoolService.executeAsync(totalConnections::decrementAndGet, TaskPriority.LOW);
    }



    public long getTotalBytesReceived()
    {
        return totalBytesReceived.longValue();
    }
}
