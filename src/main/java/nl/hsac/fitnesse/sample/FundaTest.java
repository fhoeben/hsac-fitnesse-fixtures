package nl.hsac.fitnesse.sample;

import nl.hsac.fitnesse.fixture.slim.web.BrowserTest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fixture class customized to test funda.nl.
 */
public class FundaTest extends BrowserTest {
    private static final Pattern COUNT_PATTERN = Pattern.compile("\\((\\d+)\\)");
    public int numberOfPhotos() {
        String countText = textByXPath("//span[@class='hits']");
        Matcher m = COUNT_PATTERN.matcher(countText);
        if (!m.matches()) {
            throw new RuntimeException("Unable to determine photo count from: " + countText);
        }
        String counterStr = m.group(1);
        return Integer.parseInt(counterStr);
    }
}
