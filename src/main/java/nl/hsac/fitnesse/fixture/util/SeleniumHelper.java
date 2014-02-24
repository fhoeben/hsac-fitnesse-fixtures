package nl.hsac.fitnesse.fixture.util;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Helper to work with Selenium.
 */
public class SeleniumHelper {
    /** Default time in seconds the wait web driver waits unit throwing TimeOutException. */
    public static final int DEFAULT_TIMEOUT_SECONDS = 10;
    private WebDriver webDriver;
    private WebDriverWait webDriverWait;
    private boolean shutdownHookEnabled = false;

    /**
     * Sets up webDriver to be used.
     * @param aWebDriver web driver to use.
     */
    public void setWebDriver(WebDriver aWebDriver) {
        if (webDriver != null && !webDriver.equals(aWebDriver)) {
            webDriver.quit();
        }
        webDriver = aWebDriver;

        if (webDriver == null) {
            webDriverWait = null;
        } else {
            webDriverWait = new WebDriverWait(webDriver, DEFAULT_TIMEOUT_SECONDS);
        }

        if (!shutdownHookEnabled) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    close();
                }
            });
            shutdownHookEnabled = true;
        }
    }

    /**
     * Shuts down selenium web driver.
     */
    public void close() {
        setWebDriver(null);
    }

    /**
     * @return current page title.
     */
    public String getPageTitle() {
        return driver().getTitle();
    }

    /**
     * @return Selenium's navigation.
     */
    public WebDriver.Navigation navigate() {
        return driver().navigate();
    }

    /**
     * Finds element, by searching in multiple locations.
     * @param place identifier for element.
     * @return first element found, null if none could be found.
     */
    public WebElement getElement(String place) {
        WebElement element = null;
        if (element == null) {
            element = getElementByLabelOccurrence(place, 1);
        }
        if (element == null) {
            element = findElement(byXpath("//input[@value='%s']", place));
        }
        if (element == null) {
            element = findElement(byXpath("//button[normalize-space(text())='%s']", place));
        }
        if (element == null) {
            element = findElement(By.linkText(place));
        }
        if (element == null) {
            element = findElement(By.name(place));
        }
        if (element == null) {
            element = findElement(By.id(place));
        }
        if (element == null) {
            element = getElementByPartialLabelOccurrence(place, 1);
        }
        if (element == null) {
            element = findElement(byXpath("//input[contains(@value, '%s')]", place));
        }
        if (element == null) {
            element = findElement(By.partialLinkText(place));
        }
        return element;
    }

    /**
     * Finds element based on the exact (aria-)label text.
     * @param labelText text for label.
     * @param index occurrence of label (first is 1).
     * @return element found if any, null otherwise.
     */
    public WebElement getElementByLabelOccurrence(String labelText, int index) {
        return getElementByLabel(labelText,
                                    indexedXPath("//label[normalize-space(text())='%s']", index),
                                    indexedXPath("//*[@aria-label='%s']", index));
    }

    /**
     * Finds element based on the start of the (aria-)label text.
     * @param labelText text for label.
     * @param index occurrence of label (first is 1).
     * @return element found if any, null otherwise.
     */
    public WebElement getElementByStartLabelOccurrence(String labelText, int index) {
        return getElementByLabel(labelText,
                                    indexedXPath("//label[starts-with(normalize-space(text()), '%s')]", index),
                                    indexedXPath("//*[starts-with(@aria-label, '%s')]", index));
    }

    /**
     * Finds element based on part of the (aria-)label text.
     * @param labelText text for label.
     * @param index occurrence of label (first is 1).
     * @return element found if any, null otherwise.
     */
    public WebElement getElementByPartialLabelOccurrence(String labelText, int index) {
        return getElementByLabel(labelText,
                indexedXPath("//label[contains(normalize-space(text()), '%s')]", index),
                indexedXPath("//*[contains(@aria-label, '%s')]", index));
    }

    private String indexedXPath(String xpathBase, int index) {
        return String.format("(%s)[%s]", xpathBase, index);
    }

    private WebElement getElementByLabel(String labelText, String firstXPath, String secondXPath) {
        WebElement element = null;
        WebElement label = findElement(byXpath(firstXPath, labelText));
        if (label != null) {
            String forAttr = label.getAttribute("for");
            element = findElement(By.id(forAttr));
        }
        if (element == null) {
            element = findElement(byXpath(secondXPath, labelText));
        }
        return element;
    }

    /**
     * Sets value of hidden input field.
     * @param idOrName id or name of input field to set.
     * @param value value to set.
     * @return whether input field was found.
     */
    public boolean setHiddenInputValue(String idOrName, String value) {
        WebElement element = findElement(By.id(idOrName));
        if (element != null) {
            executeJavascript("document.getElementById('%s').value='%s'", idOrName, value);
        }
        if (element == null) {
            element = findElement(By.name(idOrName));
            if (element != null) {
                executeJavascript("document.getElementsByName('%s')[0].value='%s'", idOrName, value);
            }
        }
        return element != null;
    }

    /**
     * Executes Javascript in browser. If statementPattern contains the magic variable 'arguments'
     * the parameters will also be passed to the statement. In the latter case the parameters
     * must be a number, a boolean, a String, WebElement, or a List of any combination of the above.
     * @see http://selenium.googlecode.com/git/docs/api/java/org/openqa/selenium/JavascriptExecutor.html#executeScript(java.lang.String,%20java.lang.Object...)
     * @param statementPattern javascript to run, possibly with placeholders to be replaced.
     * @param parameters placeholder values that should be replaced before executing the script.
     * @return return value from statement.
     */
    public Object executeJavascript(String statementPattern, Object... parameters) {
        Object result;
        String script = String.format(statementPattern, parameters);
        JavascriptExecutor jse = (JavascriptExecutor) driver();
        if (statementPattern.contains("arguments")) {
            result = jse.executeScript(script, parameters);
        } else {
            result = jse.executeScript(script);
        }
        return result;
    }

    /**
     * Creates By based on xPath, supporting placeholder replacement.
     * @param pattern basic XPATH, possibly with placeholders.
     * @param parameters values for placeholders.
     * @return ByXPath.
     */
    public By byXpath(String pattern, String... parameters) {
        boolean containsSingleQuote = false;
        boolean containsDoubleQuote = false;
        Object[] escapedParams = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            String param = parameters[i];
            containsSingleQuote = containsSingleQuote || param.contains("'");
            containsDoubleQuote = containsDoubleQuote || param.contains("\"");
            escapedParams[i] = param;
        }
        if (containsDoubleQuote && containsSingleQuote) {
            throw new RuntimeException("Unsupported combination of single and double quotes");
        }
        String patternToUse;
        if (containsSingleQuote) {
            patternToUse = pattern.replace("'", "\"");
        } else {
            patternToUse = pattern;
        }
        String xpath = String.format(patternToUse, escapedParams);
        return By.xpath(xpath);
    }

    /**
     * Sets how long to wait before deciding an element does not exists.
     * @param implicitWait time in milliseconds to wait.
     */
    public void setImplicitlyWait(int implicitWait) {
        driver().manage().timeouts().implicitlyWait(implicitWait, TimeUnit.MILLISECONDS);
    }

    /**
     * Sets how long to wait on opening a page.
     * @param pageLoadWait time in milliseconds to wait.
     */
    public void setPageLoadWait(int pageLoadWait) {
        driver().manage().timeouts().pageLoadTimeout(pageLoadWait, TimeUnit.MILLISECONDS);
    }

    /**
     * Finds first element matching the By supplied.
     * @param by criteria.
     * @return element if found, null if none could be found.
     */
    public WebElement findElement(By by) {
        return findElement(false, by);
    }

    /**
     * Finds element matching the By supplied.
     * @param atMostOne true indicates multiple matching elements should trigger an exception
     * @param by criteria.
     * @return element if found, null if none could be found.
     * @throws RuntimeException if atMostOne is true and multiple elements match by.
     */
    public WebElement findElement(boolean atMostOne, By by) {
        return findElement(driver(), atMostOne, by);
    }

    /**
     * Allows direct access to WebDriver. If possible please use methods of this class to facilitate testing.
     * @return selenium web driver.
     */
    public WebDriver driver() {
        return webDriver;
    }

    /**
     * Allows clients to wait until a certain condition is true.
     * @return wait using the driver in this helper.
     */
    public WebDriverWait waitDriver() {
        return webDriverWait;
    }

    /**
     * Finds element matching the By supplied.
     * @param context context to find element in.
     * @param atMostOne true indicates multiple matching elements should trigger an exception
     * @param by criteria.
     * @return element if found, null if none could be found.
     * @throws RuntimeException if atMostOne is true and multiple elements match by.
     */
    private WebElement findElement(SearchContext context, boolean atMostOne, By by) {
        WebElement element = null;
        List<WebElement> elements = context.findElements(by);
        if (elements.size() == 1) {
            element = elements.get(0);
        } else if (elements.size() > 1) {
            if (!atMostOne) {
                element = elements.get(0);
            } else {
                elements = elementsWithId(elements);
                if (elements.size() == 1) {
                    element = elements.get(0);
                } else {
                    throw new RuntimeException("Multiple elements with id found for: " + by
                                                + ":\n" + elementsAsString(elements));
                }
            }
        }
        return element;
    }

    private List<WebElement> elementsWithId(List<WebElement> elements) {
        List<WebElement> result = new ArrayList<WebElement>(1);
        for (WebElement e : elements) {
            String attr = e.getAttribute("id");
            if (attr != null && !attr.isEmpty()) {
                result.add(e);
            }
        }
        return result;
    }

    private String elementsAsString(Collection<WebElement> elements) {
        StringBuilder b = new StringBuilder();
        b.append("[");
        boolean first = true;
        for (WebElement e : elements) {
            if (first) {
                first = false;
            } else {
                b.append(", ");
            }
            b.append(e.getAttribute("id"));
        }
        b.append("]");
        return b.toString();
    }

    /**
     * Takes screenshot of current page (as .png).
     * @param baseName name for file created (without extension),
     *                 if a file already exists with the supplied name an
     *                 '_index' will be added.
     * @return absolute path of file created.
     */
    public String takeScreenshot(String baseName) {
        String result = null;
        WebDriver d = driver();
        if (!(d instanceof TakesScreenshot)) {
            d = new Augmenter().augment(d);
        }
        if (d instanceof TakesScreenshot) {
            TakesScreenshot ts = (TakesScreenshot) d;
            byte[] png = ts.getScreenshotAs(OutputType.BYTES);
            result = writeScreenshot(baseName, png);
        }
        return result;
    }

    private String writeScreenshot(String baseName, byte[] png) {
        String result;
        File output = determineFilename(baseName);
        FileOutputStream target = null;
        try {
            target = new FileOutputStream(output);
            target.write(png);
            result = output.getAbsolutePath();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (target != null) {
                try {
                    target.close();
                } catch (IOException ex) {
                    // what to to?
                }
            }
        }
        return result;
    }

    private File determineFilename(String baseName) {
        File output = new File(baseName + ".png");
        // ensure directory exists
        File parent = output.getAbsoluteFile().getParentFile();
        if (!parent.exists()) {
            if (!parent.mkdirs()) {
                throw new IllegalArgumentException(
                        "Unable to create directory: "
                                + parent.getAbsolutePath());
            }
        }
        int i = 0;
        // determine first filename not yet in use.
        while (output.exists()) {
            i++;
            String name = String.format(baseName + "_%s.png", i);
            output = new File(name);
        }
        return output;
    }

    public WebDriver getWebDriver() {
        return webDriver;
    }
}
