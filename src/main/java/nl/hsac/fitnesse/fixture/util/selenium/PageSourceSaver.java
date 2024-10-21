package nl.hsac.fitnesse.fixture.util.selenium;

import nl.hsac.fitnesse.fixture.Environment;
import nl.hsac.fitnesse.fixture.util.FileUtil;
import nl.hsac.fitnesse.fixture.util.selenium.by.ConstantBy;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Saves page source to disk.
 */
public class PageSourceSaver {
    private static final Logger LOGGER = LoggerFactory.getLogger(PageSourceSaver.class);
    private static final Pattern BASE_TAG_EXPR = Pattern.compile("<base\\s+href=.+?>", Pattern.CASE_INSENSITIVE);
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
        String source = getCurrentFrameSource(sourceReplacements);
        if (!framesWithFakeSources.isEmpty()) {
            // replace fake_src by src
            source = source.replace(" " + FAKE_SRC_ATTR + "=", " src=");
            removeFakeSourceAttr(framesWithFakeSources);
        }
        return saveSourceAsPageSource(fileName, source);
    }

    protected String saveFrameSource(WebElement frame) {
        SeleniumHelper helper = getSeleniumHelper();
        SearchContext currentContext = helper.getCurrentContext();
        try {
            helper.switchToFrame(frame);
            try {
                String fileName = helper.getResourceNameFromLocation();
                return savePageSource(fileName);
            } finally {
                helper.switchToParentFrame();
                if (currentContext != null) {
                    helper.setCurrentContext(currentContext);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error saving sources of nested (i)frame: " + frame, e);
            return null;
        }
    }

    protected String getCurrentFrameSource(Map<String, String> sourceReplacements) {
        String source = getCurrentPageSource();
        if (sourceReplacements != null && !sourceReplacements.isEmpty()) {
            source = replaceSourceOfFrames(sourceReplacements, source);

            // base tag changes behavior of relative src refs, we comment it out SO:
            // - relative links to nested iframes work
            // - it it still visible in saved html
            Matcher matcher = BASE_TAG_EXPR.matcher(source);
            if (matcher.find()) {
                source = matcher.replaceAll("<!--Commented out when saving page source $0-->");
            }
        }
        return source;
    }

    protected String replaceSourceOfFrames(Map<String, String> sourceReplacements, String html) {
        for (Map.Entry<String, String> entry : sourceReplacements.entrySet()) {
            String originalLocation = entry.getKey();
            String newLocation = entry.getValue();
            html = html.replace("src=\"" + originalLocation + "\"", "src=\"" + newLocation + "\"");
        }
        return html;
    }

    protected void addSourceReplacementsForFrame(Map<String, String> sourceReplacements, String savedLocation, String fullUrlOfFrame) {
        String fullUrlOfParent = getLocation();
        int lastSlash = fullUrlOfParent.lastIndexOf("/");
        String baseUrl = fullUrlOfParent.substring(0, lastSlash + 1);
        String relativeUrlOfFrame = fullUrlOfFrame.replace(baseUrl, "");

        String relativePathToSaved = savedLocation.replace("files/pagesources/", "");
        sourceReplacements.put(fullUrlOfFrame, relativePathToSaved);
        sourceReplacements.put(relativeUrlOfFrame, relativePathToSaved);
        String framePathAndQuery = getPathAndQuery(fullUrlOfFrame);
        if (framePathAndQuery != null) {
            sourceReplacements.put(framePathAndQuery, relativePathToSaved);
        }
    }

    protected String getPathAndQuery(String urlString) {
        String path = null;
        try {
            URL url = new URL(urlString);
            path = url.getPath();
            String query = url.getQuery();
            if (query != null) {
                path += "?" + query;
            }
        } catch (MalformedURLException e) {
            // leave path null
        }
        return path;
    }


    protected String saveSourceAsPageSource(String fileName, String source) {
        String pageSourceName = getPageSourceName(fileName);
        String ext = getPageSourceExtension();
        byte[] content = convertSourceToBytes(source);
        String file = FileUtil.saveToFile(pageSourceName, ext, content);
        String wikiUrl = getWikiUrl(file);

        String result;
        if (wikiUrl != null) {
            result = wikiUrl;
        } else {
            result = file;
        }
        return result;
    }

    protected String getPageSourceExtension() {
        return "html";
    }

    protected String getCurrentPageSource() {
        return getSeleniumHelper().getHtml();
    }

    protected byte[] convertSourceToBytes(String source) {
        try {
            return source.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unable to save source", e);
        }
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
