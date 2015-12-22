package nl.hsac.fitnesse.fixture.util.selenium;

import java.net.URL;

/**
 * Helper to deal with SauceLabs, a cloud provider for Selenium.
 */
public class SauceLabsHelper {
    /**
     * @param url remote Selenium driver URL.
     * @return true if url indicates connection to SauceLabs.
     */
    public static boolean isRelevant(URL url) {
        boolean result = false;
        if (url != null) {
            result = url.getHost().endsWith("saucelabs.com");
        }
        return result;
    }

    public static String getJobLink(URL remoteUrl, String jobId) {
        return String.format("https://saucelabs.com/jobs/%s?auth=%s",
                jobId, getAuthToken(remoteUrl, jobId));
    }

    public static String getTagToEmbedJobOverview(URL remoteUrl, String jobId) {
        return String.format("<script type=\"text/javascript\" src=\"https://saucelabs.com/job-embed/%s.js?auth=%s\"></script>",
                jobId, getAuthToken(remoteUrl, jobId));
    }

    public static String getLinkToLiveViewOfRun(URL remoteUrl, String jobId) {
        return String.format("https://saucelabs.com/manual/live_from_job/%s?auth=%s",
                jobId, getAuthToken(remoteUrl, jobId));
    }

    public static String getTagToEmbedVideoOfRun(URL remoteUrl, String jobId) {
        return String.format("<div><script src=\"https://saucelabs.com/video-embed/%s.js?auth=%s\"></script></div>",
                jobId, getAuthToken(remoteUrl, jobId));
    }

    public static String getAuthToken(URL remoteUrl, String jobId) {
        String key = remoteUrl.getUserInfo();
        return SecurityUtil.hmacEncode("HmacMD5", jobId, key);
    }

}
