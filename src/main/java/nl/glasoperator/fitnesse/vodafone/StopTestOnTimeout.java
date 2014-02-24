package nl.glasoperator.fitnesse.vodafone;

import org.openqa.selenium.TimeoutException;

/**
 * Exception to indicate to Slim that test should be aborted because of a timeout during a test step.
 */
public class StopTestOnTimeout extends RuntimeException {
    public StopTestOnTimeout(Throwable e) {
        super(e);
    }
}
