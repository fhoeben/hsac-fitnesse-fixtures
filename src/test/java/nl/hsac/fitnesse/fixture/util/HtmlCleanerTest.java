package nl.hsac.fitnesse.fixture.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class HtmlCleanerTest {
    private final HtmlCleaner cleaner = new HtmlCleaner();

    @Test
    public void testCleanUrl() {
        assertEquals("http://hallo.com/test", cleaner.getUrl("http://hallo.com/test"));
        assertEquals("http://hallo.com/test2", cleaner.getUrl("<a href=\"http://hallo.com/test2\">Hallo</a>"));
        assertEquals("http://hallo.com/test3?testparam=1", cleaner.getUrl("<a href=\"http://hallo.com/test3?testparam=1\">Hallo2</a>"));
        assertEquals("http://hallo.com/test3?testparam=1&param2=3", cleaner.getUrl("<a href=\"http://hallo.com/test3?testparam=1&amp;param2=3\">Hallo3</a>"));

        assertNull(cleaner.getUrl(null));
    }
}
