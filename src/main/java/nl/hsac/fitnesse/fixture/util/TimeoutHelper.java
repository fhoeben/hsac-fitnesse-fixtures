package nl.hsac.fitnesse.fixture.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Helper to perform tasks, while ensuring they will complete.
 */
public class TimeoutHelper {
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    /**
     * Calls task but ensures it ends.
     * @param <T> expected type of return value.
     * @param description description of task.
     * @param timeout timeout in milliseconds.
     * @param task task to execture.
     * @return return value from task.
     */
    public <T> T callWithTimeout(String description, int timeout, Callable<T> task) {
        Future<T> callFuture = threadPool.submit(task);
        return getWithTimeout(callFuture, timeout, description);
    }

    /**
     * Calls task but ensures it ends.
     * @param description description of task.
     * @param timeout timeout in milliseconds.
     * @param task task to execture.
     */
    public void callWithTimeout(String description, int timeout, Runnable task) {
        Future<?> callFuture = threadPool.submit(task);
        getWithTimeout(callFuture, timeout, description);
    }

    private <T> T getWithTimeout(Future<T> callFuture, int timeout, String description) {
        try {
            return callFuture.get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            throw new RuntimeException("Interrupted while waiting for: "
                                                + description, ex);
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new RuntimeException("Exception while calling: ", cause);
        } catch (TimeoutException ex) {
            throw new RuntimeException(description + " did not terminate within: " + timeout, ex);
        }
    }

}
