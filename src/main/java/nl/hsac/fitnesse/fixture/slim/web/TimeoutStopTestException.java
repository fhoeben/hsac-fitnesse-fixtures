package nl.hsac.fitnesse.fixture.slim.web;

import nl.hsac.fitnesse.fixture.slim.StopTestException;
import org.openqa.selenium.TimeoutException;

/**
 * Exception to indicate script should be stopped because of Selenium timeout.
 */
public class TimeoutStopTestException extends StopTestException {
    /**
     * Creates new.
     * @param message message for exception.
     * @param cause timeout exception thrown by Selenium.
     */
    public TimeoutStopTestException(String message, TimeoutException cause) {
        this(true, message, cause);
    }

    /**
     * Creates new.
     * @param stackTraceInWiki whether wiki should include the stack trace of this exception, or just the message
     * @param message message for exception.
     * @param cause timeout exception thrown by Selenium.
     */
    public TimeoutStopTestException(boolean stackTraceInWiki, String message, TimeoutException cause) {
        super(stackTraceInWiki
                    ? message
                    : String.format("message:<<%s>>", message),
                cause);
    }

    /**
     * Creates new.
     * @param cause timeout exception thrown by Selenium.
     */
    public TimeoutStopTestException(TimeoutException cause) {
        super(cause);
    }
}
