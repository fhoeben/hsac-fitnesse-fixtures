package nl.hsac.fitnesse.fixture.slim;

import org.apache.commons.lang3.StringEscapeUtils;

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
        String result = message;
        if (!stackTraceInWiki) {
            // Until https://github.com/unclebob/fitnesse/issues/731 is fixed
            if (message.contains("\n")) {
                if (!message.startsWith("<") || !message.endsWith(">")) {
                    // it is not yet HTML, make it HTML so we can use <br/>
                    message = StringEscapeUtils.escapeHtml4(message);
                    message = String.format("<div>%s</div>", message);
                }
                message = message.replaceAll("(\\r)?\\n", "<br/>");
            }
            result = String.format("message:<<%s>>", message);
        }
        return result;
    }

}
