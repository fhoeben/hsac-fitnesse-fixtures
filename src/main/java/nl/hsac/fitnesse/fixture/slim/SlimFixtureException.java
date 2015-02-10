package nl.hsac.fitnesse.fixture.slim;

/**
 * Exception in Slim fixture.
 */
public class SlimFixtureException extends RuntimeException {

    /**
     * Creates new.
     * @param message message for exception.
     */
    public SlimFixtureException(String message) {
        this(true, message);
    }

    /**
     * Creates new.
     * @param stackTraceInWiki whether wiki should include the stack trace of this exception, or just the message
     * @param message message for exception.
     */
    public SlimFixtureException(boolean stackTraceInWiki, String message) {
        super(createMessage(stackTraceInWiki, message));
    }

    /**
     * Creates new.
     * @param message message for exception.
     * @param cause underlying exception.
     */
    public SlimFixtureException(String message, Throwable cause) {
        this(true, message, cause);
    }

    /**
     * Creates new.
     * @param stackTraceInWiki whether wiki should include the stack trace of this exception, or just the message
     * @param message message for exception.
     * @param cause underlying exception.
     */
    public SlimFixtureException(boolean stackTraceInWiki, String message, Throwable cause) {
        super(createMessage(stackTraceInWiki, message), cause);
    }

    /**
     * Creates new.
     * @param cause underlying exception.
     */
    public SlimFixtureException(Throwable cause) {
        super(cause);
    }

    private static String createMessage(boolean stackTraceInWiki, String message) {
        return stackTraceInWiki
                ? message
                : String.format("message:<<%s>>", message);
    }

}
