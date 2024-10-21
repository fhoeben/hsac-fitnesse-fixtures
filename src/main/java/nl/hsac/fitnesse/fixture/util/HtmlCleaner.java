package nl.hsac.fitnesse.fixture.util;

import fitnesse.slim.Converter;
import fitnesse.slim.converters.ConverterRegistry;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper to remove wiki formatting from strings.
 */
public class HtmlCleaner {
    private static final Pattern LINKPATTERN = Pattern.compile("<a(\\s+.*?)?\\s+href=\"(.*?)\".*?>(.*?)</a>(.*)", Pattern.CASE_INSENSITIVE);
    private static final Pattern IMAGEPATTERN = Pattern.compile("<img(\\s+.*?)?\\s+src=\"(.*?)\".*?/>", Pattern.CASE_INSENSITIVE);
    private static final Pattern PRE_FORMATTED_PATTERN = Pattern.compile("<pre>\\s*(.*?)\\s*</pre>", Pattern.DOTALL);
    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlCleaner.class);
    /**
     * Gets a URL from a wiki page value.
     * @param htmlLink link as present on wiki page.
     * @return address the link points to (if it is an 'a'), the original link otherwise.
     */
    public String getUrl(String htmlLink) {
        String result = htmlLink;
        if (htmlLink != null) {
            Matcher linkMatcher = LINKPATTERN.matcher(htmlLink);
            Matcher imgMatcher = IMAGEPATTERN.matcher(htmlLink);
            if (linkMatcher.matches()) {
                String href = linkMatcher.group(2);
                href = StringEscapeUtils.unescapeHtml4(href);
                result = href + linkMatcher.group(4);
            } else if (imgMatcher.matches()) {
                String src = imgMatcher.group(2);
                result = StringEscapeUtils.unescapeHtml4(src);
            }
        }
        return result;
    }

    /**
     * Removes result of wiki formatting (for e.g. email addresses) if needed.
     * @param rawValue value as received from FitNesse.
     * @return rawValue if it was just text or any object, cleaned version if it was not.
     */
    public <T> T cleanupValue(T rawValue) {
        T cleanValue;
        if (rawValue instanceof String) {
            cleanValue = (T) cleanupValue((String) rawValue);
        } else {
            cleanValue = rawValue;
        }
        return cleanValue;
    }

    /**
     * Removes result of wiki formatting (for e.g. email addresses) if needed.
     * @param rawValue value as received from Fitnesse.
     * @return rawValue if it was just text, cleaned version if it was not.
     */
    public String cleanupValue(String rawValue) {
        String result = null;
        if (rawValue != null) {
            Matcher matcher = LINKPATTERN.matcher(rawValue);
            if (matcher.matches()) {
                result = matcher.group(3) + matcher.group(4);
            } else {
                result = cleanupPreFormatted(rawValue);
            }
        }
        return result;
    }

    /**
     * Removes HTML preformatting (if any).
     * @param value value (possibly pre-formatted)
     * @return value without HTML preformatting.
     */
    public String cleanupPreFormatted(String value) {
        String result = value;
        if (value != null) {
            Matcher matcher = PRE_FORMATTED_PATTERN.matcher(value);
            if (matcher.matches()) {
                String escapedBody = matcher.group(1);
                result = StringEscapeUtils.unescapeHtml4(escapedBody);
            }
        }
        return result;
    }

    public Object parseValue(String value) {
        Object result = value;
        try {
            Converter<Map> converter = getConverter(value);
            if (converter != null) {
                result = converter.fromString(value);
            }
        } catch (Throwable t) {
            LOGGER.error("Unable to parse value: " + value, t);
        }
        return result;
    }

    protected Converter<Map> getConverter(String cell) {
        Converter<Map> converter = null;
        if (cell.startsWith("<table class=\"hash_table\">")
                && cell.endsWith("</table>")) {
            converter = ConverterRegistry.getConverterForClass(Map.class);
        }
        return converter;
    }
}
