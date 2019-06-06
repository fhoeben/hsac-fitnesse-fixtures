package nl.hsac.fitnesse.fixture.util;

/**
 * Helper to deal with data URLs.
 * @link https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/Data_URIs
 */
public class DataUrlHelper {
    /**
     * Checks whether supplied string is a data-url.
     * @param url string to check.
     * @return true, if content is data url
     */
    public static boolean isDataUrl(String url) {
        return url.startsWith("data:")
                && url.contains(",");
    }

    /**
     * Gets data embedded in data url.
     * @param dataUrl url to extract data from.
     * @return data part of URL.
     */
    public static String getData(String dataUrl) {
        int indexOfComma = dataUrl.indexOf(',');
        return dataUrl.substring(indexOfComma + 1);
    }
}
