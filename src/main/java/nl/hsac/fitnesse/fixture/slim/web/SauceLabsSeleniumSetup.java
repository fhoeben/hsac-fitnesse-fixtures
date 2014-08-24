package nl.hsac.fitnesse.fixture.slim.web;

import nl.hsac.fitnesse.fixture.util.SecurityUtil;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.util.Map;

public class SauceLabsSeleniumSetup extends SeleniumDriverSetup {
    public static final String USER_PROPERTY = "SauceLabsUser";
    public static final String ACCESS_KEY_PROPERTY = "SauceLabsAccessKey";
    public static final String SAUCE_LABS_JOB_ID = "SauceLabsJobId";

    private String user;
    private String accessKey;
    private String driverUrlPattern = "http://%s:%s@ondemand.saucelabs.com:80/wd/hub";

    public void setUser(String aUser) {
        user = aUser;
    }

    public void setAccessKey(String anAccessKey) {
        accessKey = anAccessKey;
    }

    public boolean connectWithCapabilities(Map<String, String> capabilities) throws MalformedURLException {
        boolean result = connectToDriverAtWithCapabilities(getDriverUrl(), capabilities);
        String jobId = getJobId();
        getEnvironment().setSymbol(SAUCE_LABS_JOB_ID, jobId);
        return result;
    }

    public void setDriverUrlPattern(String aPattern) {
        driverUrlPattern = aPattern;
    }

    public String jobId() {
        return getEnvironment().getSymbol(SAUCE_LABS_JOB_ID);
    }

    private String getJobId() {
        return ((RemoteWebDriver) getHelper().driver()).getSessionId().toString();
    }

    public String jobLink() {
        String url = String.format("https://saucelabs.com/jobs/%s?auth=%s",
                jobId(), authToken());
        return String.format("<a href=\"%s\">%s</a>", url, url);
    }

    public String overviewOfRun() {
        return String.format("<div><script type=\"text/javascript\" src=\"https://saucelabs.com/job-embed/%s.js?auth=%s\"></script></div>",
               jobId(), authToken());
    }

    public String liveVideoOfRunLink() {
        String url = String.format("https://saucelabs.com/manual/live_from_job/%s?auth=%s",
                jobId(), authToken());
        return String.format("<a href=\"%s\" target=\"_blank\">%s</a>", url, url);
    }

    public String videoOfRun() {
        return String.format("<div style=\"width=%s;height=%s;\"><script src=\"https://saucelabs.com/video-embed/%s.js?auth=%s\"></script></div>",
                                "600px", "450px", jobId(), authToken());
    }

    public String authToken() {
        String key = getEnvironment().getSymbol(USER_PROPERTY) + ":" + getEnvironment().getSymbol(ACCESS_KEY_PROPERTY);
        return SecurityUtil.hmacEncode("HmacMD5", jobId(), key);
    }

    protected String getDriverUrl() {
        return String.format(driverUrlPattern, getUser(), getAccessKey());
    }

    protected String getUser() {
        String result = user;
        if (result == null || "".equals(result)) {
            result = System.getProperty(USER_PROPERTY);
        }
        if (result == null || "".equals(result)) {
            throw new RuntimeException("No user configured. Please set via Wiki or set system property: " + USER_PROPERTY);
        }
        getEnvironment().setSymbol(USER_PROPERTY, result);
        return result;
    }

    protected String getAccessKey() {
        String result = accessKey;
        if (result == null || "".equals(result)) {
            result = System.getProperty(ACCESS_KEY_PROPERTY);
        }
        if (result == null || "".equals(result)) {
            throw new RuntimeException("No access key configured. Please set via Wiki or set system property: " + ACCESS_KEY_PROPERTY);
        }
        getEnvironment().setSymbol(ACCESS_KEY_PROPERTY, result);
        return result;
    }
}
