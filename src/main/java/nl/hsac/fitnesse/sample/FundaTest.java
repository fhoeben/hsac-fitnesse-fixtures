package nl.hsac.fitnesse.sample;

import nl.hsac.fitnesse.fixture.slim.web.BrowserTest;
import nl.hsac.fitnesse.fixture.slim.web.annotation.TimeoutPolicy;
import nl.hsac.fitnesse.fixture.slim.web.annotation.WaitUntil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fixture class customized to test funda.nl.
 */
public class FundaTest extends BrowserTest {
    private static final Pattern COUNT_PATTERN = Pattern.compile("(\\d+)");
    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public Integer numberOfPhotos() {
        Integer count = null;
        String countText = valueOf("xpath=//span[@class='object-media-teaser-count']");
        if (countText != null) {
            Matcher m = COUNT_PATTERN.matcher(countText);
            if (m.matches()) {
                String counterStr = m.group(1);
                count = Integer.valueOf(counterStr);
            }
        }
        return count;
    }
}
