package nl.hsac.fitnesse.fixture.util.selenium;

import nl.hsac.fitnesse.fixture.Environment;
import nl.hsac.fitnesse.fixture.util.FileUtil;
import nl.hsac.fitnesse.fixture.util.selenium.by.ConstantBy;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Saves page source to disk.
 */
public class PageSourceSaver {
    private static final String FAKE_SRC_ATTR = "data-fake_src";
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
        List<WebElement> framesWithFakeSources = new ArrayList<>(2);
        Map<String, String> sourceReplacements = new HashMap<>();
        List<WebElement> frames = getFrames();
        for (WebElement frame : frames) {
            String newLocation = saveFrameSource(frame);
            if (newLocation != null) {
                String fullUrlOfFrame = frame.getAttribute("src");
                if (StringUtils.isEmpty(fullUrlOfFrame)) {
                    framesWithFakeSources.add(frame);
                    fullUrlOfFrame = "anonymousFrame" + frames.indexOf(frame);
                    addFakeSourceAttr(frame, fullUrlOfFrame);
                }

                addSourceReplacementsForFrame(sourceReplacements, newLocation, fullUrlOfFrame);
            }
        }
        String html = getCurrentFrameHtml(sourceReplacements);
        if (!framesWithFakeSources.isEmpty()) {
            // replace fake_src by src
            html = html.replace(" " + FAKE_SRC_ATTR + "=", " src=");
            removeFakeSourceAttr(framesWithFakeSources);
        }
        return saveHtmlAsPageSource(fileName, html);
    }

    protected String saveFrameSource(WebElement frame) {
        try {
            getSeleniumHelper().switchToFrame(frame);
            try {
                String fileName = getSeleniumHelper().getResourceNameFromLocation();
                return savePageSource(fileName);
            } finally {
                getSeleniumHelper().switchToParentFrame();
            }
        } catch (Exception e) {
            System.err.println("Error saving sources of nested (i)frame: " + frame);
            e.printStackTrace();
            return null;
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
            String file = FileUtil.saveToFile(pageSourceName, "HTML", html.getBytes("utf-8"));
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
        return getDriver().findElements(ConstantBy.frames());
    }

    protected String getLocation() {
        return getDriver().getCurrentUrl();
    }

    protected void addFakeSourceAttr(WebElement frame, String s) {
        executeJavascript("arguments[0].setAttribute(arguments[1], arguments[2]);", frame, FAKE_SRC_ATTR, s);
    }

    protected void removeFakeSourceAttr(List<WebElement> framesWithFakeSources) {
        for (WebElement frame : framesWithFakeSources) {
            executeJavascript("arguments[0].removeAttribute(arguments[1]);", frame, FAKE_SRC_ATTR);
        }
    }

    protected void executeJavascript(String pattern, Object... params) {
        try {
            getSeleniumHelper().executeJavascript(pattern, params);
        } catch (RuntimeException e) {
            // ignore exception adding/removing attributes and carry on rest of code
        }
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
