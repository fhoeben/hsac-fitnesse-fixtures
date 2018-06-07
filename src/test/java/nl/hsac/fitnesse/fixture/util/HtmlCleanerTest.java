package nl.hsac.fitnesse.fixture.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class HtmlCleanerTest {
    private final HtmlCleaner cleaner = new HtmlCleaner();

    @Test
    public void testCleanUrlNoKnownHtmlElement() {
        assertEquals("http://hallo.com/test", cleaner.getUrl("http://hallo.com/test"));
        assertNull(cleaner.getUrl(null));
        assertEquals("<ab href=\"http://hallo.com/test2\">Hallo</a>", cleaner.getUrl("<ab href=\"http://hallo.com/test2\">Hallo</a>"));
        assertEquals("<image src=\"http://hallo.com/test2\"/>", cleaner.getUrl("<image src=\"http://hallo.com/test2\"/>"));
    }

    @Test
    public void testCleanUrlA() {
        assertEquals("http://hallo.com/test2", cleaner.getUrl("<a href=\"http://hallo.com/test2\">Hallo</a>"));
        assertEquals("http://hallo.com/test3?testparam=1", cleaner.getUrl("<a href=\"http://hallo.com/test3?testparam=1\">Hallo2</a>"));
        assertEquals("http://hallo.com/test3?testparam=1&param2=3", cleaner.getUrl("<a href=\"http://hallo.com/test3?testparam=1&amp;param2=3\">Hallo3</a>"));

        assertEquals("http://hallo.com/test2123", cleaner.getUrl("<a href=\"http://hallo.com/test2\">Hallo</a>123"));

        assertEquals("http://hallo.com/test4", cleaner.getUrl("<a href=\"http://hallo.com/test4\" target=\"_blank\">Hallo</a>"));
        assertEquals("http://hallo.com/test5", cleaner.getUrl("<a   href=\"http://hallo.com/test5\"   target=\"_blank\"   >Hallo</a>"));
        assertEquals("http://hallo.com/test6", cleaner.getUrl("<a\t  target=\"_blank\"  href=\"http://hallo.com/test6\">Hallo</a>"));
    }

    @Test
    public void testCleanUrlImg() {
        assertEquals("http://hallo.com/test2", cleaner.getUrl("<img src=\"http://hallo.com/test2\"/>"));
        assertEquals("http://hallo.com/test3?testparam=1", cleaner.getUrl("<img src=\"http://hallo.com/test3?testparam=1\"/>"));
        assertEquals("http://hallo.com/test3?testparam=1&param2=3", cleaner.getUrl("<img src=\"http://hallo.com/test3?testparam=1&amp;param2=3\"/>"));

        assertEquals("http://hallo.com/test4", cleaner.getUrl("<img src=\"http://hallo.com/test4\"  title=\"image\" height=\"200\"/>"));
        assertEquals("http://hallo.com/test4", cleaner.getUrl("<img \t  src=\"http://hallo.com/test4\" />"));
        assertEquals("http://hallo.com/test4", cleaner.getUrl("<img  title=\"image\"   target=\"_blank\"  src=\"http://hallo.com/test4\" height=\"200\"/>"));
    }

    @Test
    public void testCleanupValueMailAddress() {
        assertEquals("hallo@example.com", cleaner.cleanupValue("<a href=\"mailto:hallo@example.com\">hallo@example.com</a>"));
        assertEquals("hallo2@example.com", cleaner.cleanupValue("hallo2@example.com"));
    }
}
