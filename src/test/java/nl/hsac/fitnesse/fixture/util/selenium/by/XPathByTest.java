package nl.hsac.fitnesse.fixture.util.selenium.by;

import org.junit.Test;
import static nl.hsac.fitnesse.fixture.util.selenium.by.XPathBy.getNormalizedText;
import static org.junit.Assert.assertEquals;

public class XPathByTest {
    @Test
    public void testNormalizedText() {
        assertEquals("", getNormalizedText(""));
        assertEquals(" ", getNormalizedText("   \n "));

        assertEquals("Hallo", getNormalizedText("Hallo"));
        assertEquals(" Ha l lo ", getNormalizedText("  Ha  l\n lo   "));
        assertEquals(" Ha l lo ", getNormalizedText("  Ha  l\n \u00a0lo   "));
        assertEquals(" Ha l lo ", getNormalizedText("  Ha  l\n \u00a0\u00a0\u00a0 lo   \u00a0"));
    }

}
