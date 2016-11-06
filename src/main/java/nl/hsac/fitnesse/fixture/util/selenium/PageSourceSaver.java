package nl.hsac.fitnesse.fixture.util.selenium;

import nl.hsac.fitnesse.fixture.Environment;
import nl.hsac.fitnesse.fixture.util.FileUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Saves page source to disk.
 */
public class PageSourceSaver {
    private static final By SELECTOR = By.cssSelector("iframe,frame");
    private final String pageSourceBase;
    private final Environment environment = Environment.getInstance();
    private final SeleniumHelper helper;

    /**
     * Creates new.
     * @param pageSourceBase directory where pages will be saved.
     * @param helper helper to interact with Selenium.
     */
    public PageSourceSaver(String pageSourceBase, SeleniumHelper helper) {
        this.pageSourceBase = pageSourceBase;
        this.helper = helper;
    }

    /**
     * Saves current page's source, as new file.
     * @param fileName filename to use for saved page.
     * @return wiki Url, if file was created inside wiki's files dir, absolute filename otherwise.
     */
    public String savePageSource(String fileName) {
        Map<String, String> sourceReplacements = new HashMap<>();
        List<WebElement> frames = getFrames();
        for (WebElement frame : frames) {
            String newLocation = saveFrameSource(frame);
            String fullUrlOfFrame = frame.getAttribute("src");

            addSourceReplacementsForFrame(sourceReplacements, newLocation, fullUrlOfFrame);
        }
        String html = getCurrentFrameHtml(sourceReplacements);
        return saveHtmlAsPageSource(fileName, html);
    }

    protected String saveFrameSource(WebElement frame) {
        try {
            getSeleniumHelper().switchToFrame(frame);
            String fileName = getSeleniumHelper().getResourceNameFromLocation();
            return savePageSource(fileName);
        } finally {
            getSeleniumHelper().switchToParentFrame();
        }
    }

    protected String getCurrentFrameHtml(Map<String, String> sourceReplacements) {
        String html = getSeleniumHelper().getHtml();
        if (sourceReplacements != null && !sourceReplacements.isEmpty()) {
            html = replaceSourceOfFrames(sourceReplacements, html);
        }
        return html;
    }

    protected String replaceSourceOfFrames(Map<String, String> sourceReplacements, String html) {
        for (Map.Entry<String, String> entry : sourceReplacements.entrySet()) {
            String originalLocation = entry.getKey();
            String newLocation = entry.getValue();
            html = html.replace("src=\"" + originalLocation + "\"", "src=\"/" + newLocation + "\"");
        }
        return html;
    }

    protected void addSourceReplacementsForFrame(Map<String, String> sourceReplacements, String savedLocation, String fullUrlOfFrame) {
        String fullUrlOfParent = getLocation();
        int lastSlash = fullUrlOfParent.lastIndexOf("/");
        String baseUrl = fullUrlOfParent.substring(0, lastSlash + 1);
        String relativeUrlOfFrame = fullUrlOfFrame.replace(baseUrl, "");

        sourceReplacements.put(fullUrlOfFrame, savedLocation);
        sourceReplacements.put(relativeUrlOfFrame, savedLocation);
        String framePath = getPath(fullUrlOfFrame);
        if (framePath != null) {
            sourceReplacements.put(framePath, savedLocation);
        }
    }

    protected String getPath(String urlString) {
        String path = null;
        try {
            URL url = new URL(urlString);
            path = url.getPath();
        } catch (MalformedURLException e) {
            // leave path null
        }
        return path;
    }


    protected String saveHtmlAsPageSource(String fileName, String html) {
        String result;
        try {
            String pageSourceName = getPageSourceName(fileName);
            String file = FileUtil.saveToFile(pageSourceName, "html", html.getBytes("utf-8"));
            String wikiUrl = getWikiUrl(file);
            if (wikiUrl != null) {
                result = wikiUrl;
            } else {
                result = file;
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unable to save source", e);
        }
        return result;
    }

    /**
     * Converts a file path into a relative wiki path, if the path is insides the wiki's 'files' section.
     * @param filePath path to file.
     * @return relative URL pointing to the file (so a hyperlink to it can be created).
     */
    protected String getWikiUrl(String filePath) {
        return environment.getWikiUrl(filePath);
    }

    protected List<WebElement> getFrames() {
        return getDriver().findElements(SELECTOR);
    }

    protected String getLocation() {
        return getDriver().getCurrentUrl();
    }

    protected WebDriver getDriver() {
        return getSeleniumHelper().driver();
    }


    protected String getPageSourceName(String fileName) {
        return pageSourceBase + fileName;
    }

    protected SeleniumHelper getSeleniumHelper() {
        return helper;
    }
}
