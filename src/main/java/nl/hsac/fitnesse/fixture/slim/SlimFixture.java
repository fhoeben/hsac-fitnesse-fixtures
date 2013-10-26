package nl.hsac.fitnesse.fixture.slim;

import nl.hsac.fitnesse.fixture.Environment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for Slim fixtures.
 */
public class SlimFixture {
    private static final Pattern PATTERN = Pattern.compile("<a href=\"(.*?)\">(.*?)</a>(.*)", Pattern.CASE_INSENSITIVE);
    private Environment environment = Environment.getInstance();

    /**
     * @return environment to be used.
     */
    protected Environment getEnvironment() {
        return environment;
    }

    protected String getUrl(String htmlLink) {
        String result = htmlLink;
        Matcher matcher = PATTERN.matcher(htmlLink);
        if (matcher.matches()) {
            result = matcher.group(1) + matcher.group(3);
        }
        return result;
    }

    /**
     * Removes result of wiki formatting (for e.g. email addresses) if needed.
     * @param rawValue value as received from Fitnesse.
     * @return rawValue if it was just text, cleaned version if it was not.
     */
    protected String cleanupValue(String rawValue) {
        String result = null;
        Matcher matcher = PATTERN.matcher(rawValue);
        if (matcher.matches()) {
            result = matcher.group(2);
        } else {
            result = rawValue;
        }
        return result;
    }

    public boolean waitSeconds(int i) {
        boolean result;
        try {
            Thread.sleep(1 * 1000);
            result = true;
        } catch (InterruptedException e) {
            result = false;
        }
        return result;
    }
}
