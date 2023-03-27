package MMS.EdgeRouter;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * This class is a utility class that offers other parts of the Edge Router to execute tasks asynchronously.
 * The reason we want to use a shared thread-pool over a single on is resource utilization optimization.
 * Threads are expensive, sharing them optimizes resource allocation.
 */
public class ThreadPoolService
{
    private static final int coreCount = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService workerPool = Executors.newFixedThreadPool(coreCount * 3); // to be tuned, 3 is arbitrary, but it seems to work well


    /**
     * Executes a runnable task asynchronously, no return value.
     *
     * @param task the task to be executed
     */
    public static void executeAsync(Runnable task)
    {
        workerPool.execute(task);
    }


    /**
     * Executes a runnable task asynchronously, returns a Future object that can be used to retrieve the result.
     *
     * @param task the task to be executed
     * @return a Future object that can be used to retrieve the result
     */
    public Future<?> executeAsyncWithResult(Runnable task)
    {
        return workerPool.submit(task);
    }


    /**
     * Executes a callable task asynchronously, returns a Future object that can be used to retrieve the result.
     *
     * @param task the task to be executed
     * @return a Future object that can be used to retrieve the result
     */
    public Future<?> executeAsyncWithResult(Callable<?> task)
    {
        return workerPool.submit(task);
    }


    /**
     * Shuts down the thread-pool gracefully, i.e. it waits for all tasks to finish before shutting down.
     */
    public static void shutdownGracefully()
    {
        workerPool.shutdown();
    }


    /**
     * Shuts down the thread-pool immediately, i.e. it interrupts all tasks and shuts down.
     */
    public static void shutdownNow()
    {
        workerPool.shutdownNow();
    }


}

