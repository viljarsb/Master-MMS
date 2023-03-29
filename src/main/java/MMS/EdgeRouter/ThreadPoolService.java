package MMS.EdgeRouter;


import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;
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
    private static final ExecutorService scheduledPool = Executors.newScheduledThreadPool(1); // to be tuned, 1 is arbitrary, but it seems to work well
    private static final Map<TaskPriority, Integer> PRIORITY_MAP = new EnumMap<>(TaskPriority.class);

    static
    {
        PRIORITY_MAP.put(TaskPriority.LOW, 0);
        PRIORITY_MAP.put(TaskPriority.MEDIUM, 1);
        PRIORITY_MAP.put(TaskPriority.HIGH, 2);
        PRIORITY_MAP.put(TaskPriority.CRITICAL, 3);
    }


    /**
     * Private to prevent initialization
     */
    private ThreadPoolService() {}


    /**
     * Executes the given runnable asynchronously with the specified priority.
     *
     * @param task     The runnable to execute.
     * @param priority The priority of the task.
     */
    public static void executeAsync(Runnable task, TaskPriority priority)
    {
        workerPool.execute(new PriorityTask<>(priority, task));
    }


    /**
     * Executes the given runnable asynchronously with the specified priority and returns a Future object.
     *
     * @param task     The runnable to execute.
     * @param priority The priority of the task.
     * @param <T>      The result type of the task.
     * @return A Future object representing the result of the task.
     */
    public static <T> Future<T> executeAsyncWithResult(Runnable task, TaskPriority priority)
    {
        return workerPool.submit((Runnable) new PriorityTask<>(priority, task), null);
    }


    /**
     * Executes the given callable asynchronously with the specified priority and returns a Future object.
     *
     * @param task     The callable to execute.
     * @param priority The priority of the task.
     * @param <T>      The result type of the task.
     * @return A Future object representing the result of the task.
     */
    public static <T> Future<T> executeAsyncWithResult(Callable<T> task, TaskPriority priority)
    {
        return workerPool.submit((Callable<T>) new PriorityTask<>(priority, task));
    }


    public static <T> Future<T> executeAsyncScheduled(Callable<T> task, long delay)
    {
        return null;
    }


    /**
     * Shuts down the thread pool service gracefully.
     */
    public static void shutdownGracefully()
    {
        workerPool.shutdown();
    }


    /**
     * Shuts down the thread pool service immediately.
     */
    public static void shutdownNow()
    {
        workerPool.shutdownNow();
    }


    /**
     * A task with a priority that can be compared with other priority tasks,
     * allowing the thread pool to execute more important tasks first.
     *
     * @param <T> The result type of the task.
     */
    private static class PriorityTask<T> implements Runnable, Callable<T>, Comparable<PriorityTask<T>>
    {
        private final int priority;
        private final Runnable task;
        private final Callable<T> callable;


        /**
         * Creates a new priority task with the given priority and runnable task.
         *
         * @param priority The priority of the task.
         * @param task     The runnable task.
         */
        private PriorityTask(TaskPriority priority, Runnable task)
        {
            this.priority = PRIORITY_MAP.getOrDefault(priority, 0);
            this.task = task;
            this.callable = null;
        }


        /**
         * Creates a new priority task with the given priority and callable task.
         *
         * @param priority The priority of the task.
         * @param callable The callable task.
         */
        private PriorityTask(TaskPriority priority, Callable<T> callable)
        {
            this.priority = PRIORITY_MAP.getOrDefault(priority, 0);
            this.task = null;
            this.callable = callable;
        }


        @Override
        public int compareTo(@NotNull ThreadPoolService.PriorityTask<T> o)
        {
            return Integer.compare(this.priority, o.priority);
        }


        @Override
        public void run()
        {
            if (task != null) task.run();
        }


        @Override
        public T call() throws Exception
        {
            if (callable != null) return callable.call();
            else return null;
        }
    }
}
