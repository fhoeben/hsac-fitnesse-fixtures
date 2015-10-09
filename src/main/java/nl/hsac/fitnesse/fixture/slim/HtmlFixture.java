package nl.hsac.fitnesse.fixture.slim;

import nl.hsac.fitnesse.fixture.util.HtmlCleaner;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Fixture to facilitate working with HTML (fragments) obtained from an application being tested.
 */
public class HtmlFixture extends SlimFixture {
    private final HtmlCleaner htmlCleaner = new HtmlCleaner();

    /**
     * Escapes the supplied html and places it inside a <pre></pre> block, allowing it to be shown in a wiki page.
     * @param html HTML content to show.
     * @return HTML tags that can be shown in wiki page.
     */
    public String htmlSource(String html) {
        return getEnvironment().getHtml(html);
    }

    /**
     * Unescapes supplied HTML content so it can be rendered inside a wiki page.
     * @param htmlSource HTML code to display (possibly surrounded by <pre></pre> tags).
     * @return unescaped content, enclosed in <div></div> so wiki will not escape it.
     */
    public String html(String htmlSource) {
        String cleanSource = htmlCleaner.cleanupPreFormatted(htmlSource);
        return "<div>" + StringEscapeUtils.unescapeHtml4(cleanSource) + "</div>";
    }
}
