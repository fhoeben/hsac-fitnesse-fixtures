package nl.hsac.fitnesse.fixture.slim.web;

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
        String jobId = jobId();
        getEnvironment().setSymbol(SAUCE_LABS_JOB_ID, jobId);
        return result;
    }

    public void setDriverUrlPattern(String aPattern) {
        driverUrlPattern = aPattern;
    }

    public String jobId() {
        return ((RemoteWebDriver) getHelper().driver()).getSessionId().toString();
    }

    protected String getDriverUrl() {
        return String.format(driverUrlPattern, getUser(), getAccessKey());
    }

    protected String getUser() {
        String result = user;
        if (user != null && !"".equals(user)) {
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
        if (accessKey != null && !"".equals(accessKey)) {
            result = System.getProperty(ACCESS_KEY_PROPERTY);
        }
        if (result == null || "".equals(result)) {
            throw new RuntimeException("No access key configured. Please set via Wiki or set system property: " + ACCESS_KEY_PROPERTY);
        }
        getEnvironment().setSymbol(ACCESS_KEY_PROPERTY, result);
        return result;
    }
}
