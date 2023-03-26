package MMS.EdgeRouter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolService
{
    private static final int coreCount = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService workerPool = Executors.newFixedThreadPool(coreCount * 3); // to be tuned, 3 is arbitrary, but it seems to work well

    public static ExecutorService getWorkerPool()
    {
        return workerPool;
    }
}

