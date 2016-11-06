package nl.hsac.fitnesse.fixture.util.selenium;

import nl.hsac.fitnesse.fixture.slim.StopTestException;
import nl.hsac.fitnesse.fixture.util.FileUtil;
import org.apache.commons.io.FilenameUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.internal.Base64Encoder;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.ScreenshotException;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper to work with Selenium.
 */
public class SeleniumHelper {
    /** Default time in seconds the wait web driver waits unit throwing TimeOutException. */
    private static final int DEFAULT_TIMEOUT_SECONDS = 10;

    private static final String ELEMENT_ON_SCREEN_JS =
            "if (arguments[0].getBoundingClientRect) {\n" +
                    "var rect = arguments[0].getBoundingClientRect();\n" +
                    "return (\n" +
                    "  rect.top >= 0 &&\n" +
                    "  rect.left >= 0 &&\n" +
                    "  rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) &&\n" +
                    "  rect.right <= (window.innerWidth || document.documentElement.clientWidth));\n" +
            "} else { return null; }";

    private static final String TOP_ELEMENT_AT =
            "if (arguments[0].getBoundingClientRect) {\n" +
                    "  var rect = arguments[0].getBoundingClientRect();\n" +
                    "  var x = (rect.left + rect.right)/2;\n" +
                    "  var y = (rect.top + rect.bottom)/2;\n" +
                    "  return document.elementFromPoint(x,y);\n" +
                    "} else { return null; }";

    // Regex to find our own 'fake xpath function' in xpath 'By' content
    private final static Pattern X_PATH_NORMALIZED = Pattern.compile("normalized\\((.+?(\\(\\))?)\\)");

    private final List<WebElement> currentIFramePath = new ArrayList<WebElement>(4);
    private int frameDepthOnLastAlertError;
    private DriverFactory factory;
    private WebDriver webDriver;
    private WebDriverWait webDriverWait;
    private boolean shutdownHookEnabled = false;
    private int defaultTimeoutSeconds = DEFAULT_TIMEOUT_SECONDS;

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
            webDriverWait = new WebDriverWait(webDriver, getDefaultTimeoutSeconds());
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
     * Finds element to click, by searching in multiple locations.
     * @param place identifier for element.
     * @return first interactable element found,
     *          first element found if no interactable element could be found,
     *          null if none could be found.
     */
    public WebElement getElementToClick(String place) {
        By by = placeToBy(place);
        if (by != null) {
            return findElement(by);
        } else {
            WebElement element = findElement(By.linkText(place));
            WebElement firstFound = element;
            if (!isInteractable(element)) {
                // finding by linkText does not find actual text if css text-transform is in place
                element = findByXPath(".//*[normalized(descendant::text())='%s']/ancestor-or-self::a", place);
                if (firstFound == null) {
                    firstFound = element;
                }
            }
            if (!isInteractable(element)) {
                element = findByXPath(".//button/descendant-or-self::text()[normalized(.)='%s']/ancestor-or-self::button", place);
                if (firstFound == null) {
                    firstFound = element;
                }
            }
            if (!isInteractable(element)) {
                element = findByXPath(".//label/descendant-or-self::text()[normalized(.)='%s']/ancestor-or-self::label", place);
                if (firstFound == null) {
                    firstFound = element;
                }
            }
            if (!isInteractable(element)) {
                element = getElementExact(place);
                if (firstFound == null) {
                    firstFound = element;
                }
            }
            if (("Submit".equals(place) || "Reset".equals(place))
                    && !isInteractable(element)) {
                element = findElement(byCss("input[type='%s']:not([value])", place.toLowerCase()));
                if (firstFound == null) {
                    firstFound = element;
                }
            }
            if (!isInteractable(element)) {
                element = findElement(By.partialLinkText(place));
                if (firstFound == null) {
                    firstFound = element;
                }
            }
            if (!isInteractable(element)) {
                // finding by linkText does not find actual text if css text-transform is in place
                element = findByXPath(".//*[contains(normalized(descendant::text()),'%s')]/ancestor-or-self::a", place);
                if (firstFound == null) {
                    firstFound = element;
                }
            }
            if (!isInteractable(element)) {
                element = findByXPath(".//button/descendant-or-self::text()[contains(normalized(.), '%s')]/ancestor-or-self::button", place);
                if (firstFound == null) {
                    firstFound = element;
                }
            }
            if (!isInteractable(element)) {
                element = findByXPath(".//label/descendant-or-self::text()[contains(normalized(.), '%s')]/ancestor-or-self::label", place);
                if (firstFound == null) {
                    firstFound = element;
                }
            }
            if (!isInteractable(element)) {
                element = getElementPartial(place);
                if (firstFound == null) {
                    firstFound = element;
                }
            }
            if (!isInteractable(element)) {
                // find element with specified text and 'onclick' attribute
                element = findByXPath(".//*[@onclick and normalized(text())='%s']", place);
                if (firstFound == null) {
                    firstFound = element;
                }
            }
            if (!isInteractable(element)) {
                element = findByXPath(".//*[@onclick and contains(normalized(text()),'%s')]", place);
                if (firstFound == null) {
                    firstFound = element;
                }
            }
            if (!isInteractable(element)) {
                // find element with child with specified text and 'onclick' attribute
                element = findByXPath(".//*[@onclick and normalized(descendant::text())='%s']", place);
                if (firstFound == null) {
                    firstFound = element;
                }
            }
            if (!isInteractable(element)) {
                element = findByXPath(".//*[@onclick and contains(normalized(descendant::text()),'%s')]", place);
                if (firstFound == null) {
                    firstFound = element;
                }
            }
            if (!isInteractable(element)) {
                // find element with specified text
                element = findByXPath(".//*[normalized(text())='%s']", place);
                if (firstFound == null) {
                    firstFound = element;
                }
            }
            if (!isInteractable(element)) {
                element = findByXPath(".//*[contains(normalized(text()),'%s')]", place);
                if (firstFound == null) {
                    firstFound = element;
                }
            }
            if (!isInteractable(element)) {
                // find element with child with specified text
                element = findByXPath(".//*[normalized(descendant::text())='%s']", place);
                if (firstFound == null) {
                    firstFound = element;
                }
            }
            if (!isInteractable(element)) {
                element = findByXPath(".//*[contains(normalized(descendant::text()),'%s')]", place);
                if (firstFound == null) {
                    firstFound = element;
                }
            }
            return isInteractable(element)
                    ? element
                    : firstFound;
        }
    }

    /**
     * Finds element to retrieve content from or enter content in, by searching in multiple locations.
     * @param place identifier for element.
     * @return first interactable element found,
     *          first element found if no interactable element could be found,
     *          null if none could be found.
     */
    public WebElement getElement(String place) {
        By by = placeToBy(place);
        if (by != null) {
            return findElement(by);
        } else {
            WebElement element = getElementExact(place);
            // first element found, even if it is not (yet) interactable.
            WebElement firstElement = element;
            if (!isInteractable(element)) {
                element = getElementPartial(place);
                if (firstElement == null) {
                    firstElement = element;
                }
            }
            return isInteractable(element)
                    ? element
                    : firstElement;
        }
    }

    public By placeToBy(String place) {
        By result = null;
        if (place.startsWith("id=")) {
            result = By.id(place.substring(3));
        } else if (place.startsWith("css=")) {
            result = By.cssSelector(place.substring(4));
        } else if (place.startsWith("name=")) {
            result = By.name(place.substring(5));
        } else if (place.startsWith("link=")) {
            result = By.linkText(place.substring(5));
        } else if (place.startsWith("partialLink=")) {
            result = By.partialLinkText(place.substring(12));
        } else if (place.startsWith("xpath=")) {
            result = By.xpath(place.substring(6));
        }
        return result;
    }

    public WebElement getElementExact(String place) {
        WebElement element = getElementByLabelOccurrence(place, -1);
        // first element found, even if it is not (yet) interactable.
        WebElement firstElement = element;

        if (!isInteractable(element)) {
            element = findElement(byCss("input[placeholder='%s']", place));
            if (firstElement == null) {
                firstElement = element;
            }
        }
        if (!isInteractable(element)) {
            element = findElement(byCss("input[value='%s']:not([type='hidden'])", place));
            if (firstElement == null) {
                firstElement = element;
            }
        }
        if (!isInteractable(element)) {
            element = findElement(byCss("textarea[placeholder='%s']", place));
            if (firstElement == null) {
                firstElement = element;
            }
        }
        if (!isInteractable(element)) {
            element = findByXPath(".//th/descendant-or-self::text()[normalized(.)='%s']/ancestor-or-self::th[1]/../td ", place);
            if (firstElement == null) {
                firstElement = element;
            }
        }
        if (!isInteractable(element)) {
            element = findByXPath(".//dt/descendant-or-self::text()[normalized(.)='%s']/ancestor-or-self::dt[1]/following-sibling::dd[1] ", place);
            if (firstElement == null) {
                firstElement = element;
            }
        }
        if (!isInteractable(element)) {
            element = getElementByAriaLabel(place, -1);
            if (firstElement == null) {
                firstElement = element;
            }
        }
        if (!isInteractable(element)) {
            element = findElement(byCss("[title='%s']", place));
            if (firstElement == null) {
                firstElement = element;
            }
        }
        return isInteractable(element)
                ? element
                : firstElement;
    }

    public WebElement getElementPartial(String place) {
        WebElement element = getElementByPartialLabelOccurrence(place, -1);
        // first element found, even if it is not (yet) interactable.
        WebElement firstElement = element;

        if (!isInteractable(element)) {
            element = findElement(byCss("input[placeholder*='%s']", place));
            if (firstElement == null) {
                firstElement = element;
            }
        }
        if (!isInteractable(element)) {
            element = findElement(byCss("input[value*='%s']:not([type='hidden'])", place));
            if (firstElement == null) {
                firstElement = element;
            }
        }
        if (!isInteractable(element)) {
            element = findElement(byCss("textarea[placeholder*='%s']", place));
            if (firstElement == null) {
                firstElement = element;
            }
        }
        if (!isInteractable(element)) {
            element = findByXPath(".//th/descendant-or-self::text()[contains(normalized(.), '%s')]/ancestor-or-self::th[1]/../td ", place);
            if (firstElement == null) {
                firstElement = element;
            }
        }
        if (!isInteractable(element)) {
            element = findByXPath(".//dt/descendant-or-self::text()[contains(normalized(.), '%s')]/ancestor-or-self::dt[1]/following-sibling::dd[1] ", place);
            if (firstElement == null) {
                firstElement = element;
            }
        }
        if (!isInteractable(element)) {
            element = getElementByPartialAriaLabel(place, -1);
            if (firstElement == null) {
                firstElement = element;
            }
        }
        if (!isInteractable(element)) {
            element = findElement(byCss("[title*='%s']", place));
            if (firstElement == null) {
                firstElement = element;
            }
        }
        return isInteractable(element)
                ? element
                : firstElement;
    }

    /**
     * @param element element to check.
     * @return whether the element is displayed and enabled.
     */
    public boolean isInteractable(WebElement element) {
        return element != null && element.isDisplayed() && element.isEnabled();
    }

    /**
     * Finds element based on the exact (aria-)label text.
     * @param labelText text for label.
     * @param index occurrence of label (first is 1).
     * @return first interactable element found,
     *          first element found if no interactable element could be found,
     *          null if none could be found.
     */
    public WebElement getElementByLabelOccurrence(String labelText, int index) {
        return getElementByLabel(labelText, index,
                                    ".//label/descendant-or-self::text()[normalized(.)='%s']/ancestor-or-self::label"
        );
    }

    /**
     * Finds element based on the start of the (aria-)label text.
     * @param labelText text for label.
     * @param index occurrence of label (first is 1).
     * @return first interactable element found,
     *          first element found if no interactable element could be found,
     *          null if none could be found.
     */
    public WebElement getElementByStartLabelOccurrence(String labelText, int index) {
        return getElementByLabel(labelText, index,
                ".//label/descendant-or-self::text()[starts-with(normalized(.), '%s')]/ancestor-or-self::label"
        );
    }

    /**
     * Finds element based on part of the (aria-)label text.
     * @param labelText text for label.
     * @param index occurrence of label (first is 1).
     * @return first interactable element found,
     *          first element found if no interactable element could be found,
     *          null if none could be found.
     */
    public WebElement getElementByPartialLabelOccurrence(String labelText, int index) {
        return getElementByLabel(labelText, index,
                ".//label/descendant-or-self::text()[contains(normalized(.), '%s')]/ancestor-or-self::label"
        );
    }

    private String indexedXPath(String xpathBase, int index) {

        String xPath = xpathBase;
        if (index > 0) {
            xPath = String.format("(%s)[%s]", xpathBase, index);
        }
        return xPath;
    }

    private WebElement getElementByLabel(String labelText, int index, String labelXPath) {
        WebElement element = null;
        String labelPattern = indexedXPath(labelXPath, index);
        WebElement label = findByXPath(labelPattern, labelText);
        if (label != null) {
            String forAttr = label.getAttribute("for");
            if (forAttr == null || "".equals(forAttr)) {
                element = getNestedElementForValue(label);
            } else {
                element = findElement(By.id(forAttr));
            }
        }
        return element;
    }

    public WebElement getElementByAriaLabel(String labelText, int index) {
        // see if there is an element with labelText as text, whose id is referenced by an aria-labelledby attribute
        String labelledByPattern = indexedXPath(".//*[@aria-labelledby and @aria-labelledby=//*[@id]/descendant-or-self::text()[normalized(.) = '%s']/ancestor-or-self::*[@id]/@id]", index);
        WebElement element = findByXPath(labelledByPattern, labelText);
        WebElement firstFound = element;

        if (!isInteractable(element)) {
            By by = byCss("[aria-label='%s']", labelText);
            if (index > 0) {
                element = findElement(by, index - 1);
            } else {
                element = findElement(by);
            }
            if (firstFound == null) {
                firstFound = element;
            }
        }
        return isInteractable(element)
                ? element
                : firstFound;
    }

    public WebElement getElementByPartialAriaLabel(String labelText, int index) {
        String labelledByPattern = indexedXPath(".//*[@aria-labelledby and @aria-labelledby=//*[@id]/descendant-or-self::text()[contains(normalized(.), '%s')]/ancestor-or-self::*[@id]/@id]", index);
        WebElement element = findByXPath(labelledByPattern, labelText);
        WebElement firstFound = element;

        if (!isInteractable(element)) {
            By by = byCss("[aria-label*='%s']", labelText);
            if (index > 0) {
                element = findElement(by, index - 1);
            } else {
                element = findElement(by);
            }
            if (firstFound == null) {
                firstFound = element;
            }
        }
        return isInteractable(element)
                ? element
                : firstFound;
    }

    public WebElement getNestedElementForValue(WebElement parent) {
        return findElement(parent, false, By.xpath(".//input|.//select|.//textarea"));
    }

    /**
     * Determines number displayed for item in ordered list.
     * @param element ordered list item.
     * @return number, if one could be determined.
     */
    public Integer getNumberFor(WebElement element) {
        Integer number = null;
        if ("li".equalsIgnoreCase(element.getTagName())
                && element.isDisplayed()) {
            int num;
            String ownVal = element.getAttribute("value");
            if (ownVal != null && !"0".equals(ownVal)) {
                num = toInt(ownVal, 0);
            } else {
                String start = element.findElement(By.xpath("ancestor::ol")).getAttribute("start");
                num = toInt(start, 1);

                List<WebElement> allItems = element.findElements(By.xpath("ancestor::ol/li"));
                int index = allItems.indexOf(element);
                for (int i = 0; i < index; i++) {
                    WebElement item = allItems.get(i);
                    if (item.isDisplayed()) {
                        num++;
                        String val = item.getAttribute("value");
                        int valNum = toInt(val, num);
                        if (valNum != 0) {
                            num = valNum + 1;
                        }
                    }
                }
            }
            number = num;
        }
        return number;
    }

    private int toInt(String attributeValue, int defaultVal) {
        int result = defaultVal;
        if (attributeValue != null) {
            try {
                result = Integer.parseInt(attributeValue);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Unable to parse value: " + attributeValue, e);
            }
        }
        return result;
    }

    /**
     * Returns the texts of all available options for the supplied select element.
     * @param element select element to find options for.
     * @return text per option.
     */
    public ArrayList<String> getAvailableOptions(WebElement element) {
        ArrayList<String> result = null;
        if (isInteractable(element)
                && "select".equalsIgnoreCase(element.getTagName())) {
            result = new ArrayList<String>();
            List<WebElement> options = element.findElements(By.tagName("option"));
            for (WebElement option : options) {
                if (option.isEnabled()) {
                    result.add(option.getText());
                }
            }
        }
        return result;
    }

    /**
     * Sets value of hidden input field.
     * @param idOrName id or name of input field to set.
     * @param value value to set.
     * @return whether input field was found.
     */
    public boolean setHiddenInputValue(String idOrName, String value) {
        WebElement element = findElement(By.id(idOrName));
        if (element == null) {
            element = findElement(By.name(idOrName));
            if (element != null) {
                executeJavascript("document.getElementsByName('%s')[0].value='%s'", idOrName, value);
            }
        } else {
            executeJavascript("document.getElementById('%s').value='%s'", idOrName, value);
        }
        return element != null;
    }

    /**
     * Executes Javascript in browser. If statementPattern contains the magic variable 'arguments'
     * the parameters will also be passed to the statement. In the latter case the parameters
     * must be a number, a boolean, a String, WebElement, or a List of any combination of the above.
     * @link http://selenium.googlecode.com/git/docs/api/java/org/openqa/selenium/JavascriptExecutor.html#executeScript(java.lang.String,%20java.lang.Object...)
     * @param statementPattern javascript to run, possibly with placeholders to be replaced.
     * @param parameters placeholder values that should be replaced before executing the script.
     * @return return value from statement.
     */
    public Object executeJavascript(String statementPattern, Object... parameters) {
        Object result;
        String script = String.format(statementPattern, parameters);
        if (statementPattern.contains("arguments")) {
            result = executeScript(script, parameters);
        } else {
            result = executeScript(script);
        }
        return result;
    }

    protected Object executeScript(String script, Object... parameters) {
        Object result;
        JavascriptExecutor jse = (JavascriptExecutor) driver();
        try {
            result = jse.executeScript(script, parameters);
        } catch (WebDriverException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("Detected a page unload event; script execution does not work across page loads.")) {
                // page reloaded while script ran, retry it once
                result = jse.executeScript(script, parameters);
            } else {
                throw e;
            }
        }
        return result;
    }

    /**
     * Executes Javascript in browser and then waits for 'callback' to be invoked.
     * If statementPattern should reference the magic (function) variable 'callback' which should be
     * called to provide this method's result.
     * If the statementPattern contains the magic variable 'arguments'
     * the parameters will also be passed to the statement. In the latter case the parameters
     * must be a number, a boolean, a String, WebElement, or a List of any combination of the above.
     * @link http://selenium.googlecode.com/git/docs/api/java/org/openqa/selenium/JavascriptExecutor.html#executeAsyncScript(java.lang.String,%20java.lang.Object...)
     * @param statementPattern javascript to run, possibly with placeholders to be replaced.
     * @param parameters placeholder values that should be replaced before executing the script.
     * @return return value from statement.
     */
    public Object waitForJavascriptCallback(String statementPattern, Object... parameters) {
        Object result;
        String script = "var callback = arguments[arguments.length - 1];"
                        + String.format(statementPattern, parameters);
        JavascriptExecutor jse = (JavascriptExecutor) driver();
        if (statementPattern.contains("arguments")) {
            result = jse.executeAsyncScript(script, parameters);
        } else {
            result = jse.executeAsyncScript(script);
        }
        return result;
    }

    /**
     * Creates By based on CSS selector, supporting placeholder replacement.
     * @param pattern basic CSS selectot, possibly with placeholders.
     * @param parameters values for placeholders.
     * @return ByCssSelector.
     */
    public By byCss(String pattern, String... parameters) {
        String selector = fillPattern(pattern, parameters);
        return By.cssSelector(selector);
    }

    /**
     * Creates By based on xPath, supporting placeholder replacement.
     * It also supports the fictional 'normalized()' function that does whitespace normalization, that also
     * considers a '&nbsp;' whitespace.
     * @param pattern basic XPATH, possibly with placeholders.
     * @param parameters values for placeholders.
     * @return ByXPath.
     */
    public By byXpath(String pattern, String... parameters) {
        pattern = replaceNormalizedFunction(pattern);
        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = replaceNormalizedFunction(parameters[i]);
        }
        String xpath = fillPattern(pattern, parameters);
        return By.xpath(xpath);
    }

    private String replaceNormalizedFunction(String xPath) {
        if (xPath.contains("normalized(")) {
            /*
                we first check whether the pattern contains the function name, to not have the overhead of
                regex replacement when it is not needed.
            */
            Matcher m = X_PATH_NORMALIZED.matcher(xPath);
            String updatedPattern = m.replaceAll("normalize-space(translate($1, '\u00a0', ' '))");
            xPath = updatedPattern;
        }
        return xPath;
    }

    public By byJavascript(String pattern, Object... arguments) {
        return new JavascriptBy(pattern, arguments);
    }

    /**
     * Fills in placeholders in pattern using the supplied parameters.
     * @param pattern pattern to fill (in String.format style).
     * @param parameters parameters to use.
     * @return filled in pattern.
     */
    protected String fillPattern(String pattern, String[] parameters) {
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
        return String.format(patternToUse, escapedParams);
    }

    /**
     * Checks whether element is in browser's viewport.
     * @param element element to check
     * @return true if element is in browser's viewport, null if we could not determine whether it was in viewport.
     */
    public Boolean isElementOnScreen(WebElement element) {
        return (Boolean)executeJavascript(ELEMENT_ON_SCREEN_JS, element);
    }

    /**
     * Sets how long to wait before deciding an element does not exists.
     * @param implicitWait time in milliseconds to wait.
     */
    public void setImplicitlyWait(int implicitWait) {
        try {
            driver().manage().timeouts().implicitlyWait(implicitWait, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            // https://code.google.com/p/selenium/issues/detail?id=6015
            System.err.println("Unable to set implicit timeout (known issue for Safari): " + e.getMessage());
        }
    }

    /**
     * Sets how long to wait when executing asynchronous script calls.
     * @param scriptTimeout time in milliseconds to wait.
     */
    public void setScriptWait(int scriptTimeout) {
        try {
            driver().manage().timeouts().setScriptTimeout(scriptTimeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            // https://code.google.com/p/selenium/issues/detail?id=6015
            System.err.println("Unable to set script timeout (known issue for Safari): " + e.getMessage());
        }
    }

    /**
     * Sets how long to wait on opening a page.
     * @param pageLoadWait time in milliseconds to wait.
     */
    public void setPageLoadWait(int pageLoadWait) {
        try {
            driver().manage().timeouts().pageLoadTimeout(pageLoadWait, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            // https://code.google.com/p/selenium/issues/detail?id=6015
            System.err.println("Unable to set page load timeout (known issue for Safari): " + e.getMessage());
        }
    }

    /**
     * Simulates placing the mouse over the supplied element.
     * @param element element to place mouse over.
     */
    public void hoverOver(WebElement element) {
        new Actions(driver()).moveToElement(element).perform();
    }

    /**
     * @return currently active element.
     */
    public WebElement getActiveElement() {
        return getTargetLocator().activeElement();
    }

    /**
     * Finds element using xPath, supporting placeholder replacement.
     * @param pattern basic XPATH, possibly with placeholders.
     * @param parameters values for placeholders.
     * @return element if found, null if none could be found.
     */
    public WebElement findByXPath(String pattern, String... parameters) {
        By by = byXpath(pattern, parameters);
        return findElement(by);
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
        return findElement(getCurrentContext(), atMostOne, by);
    }

    private SearchContext currentContext;

    public void setCurrentContext(SearchContext currentContext) {
        this.currentContext = currentContext;
    }

    public SearchContext getCurrentContext() {
        return currentContext != null? currentContext : driver();
    }

    /**
     * Finds the nth element matching the By supplied.
     * @param by criteria.
     * @param index (zero based) matching element to return.
     * @return element if found, null if none could be found.
     */
    public WebElement findElement(By by, int index) {
        WebElement element = null;
        List<WebElement> elements = getCurrentContext().findElements(by);
        if (elements.size() > index) {
            element = elements.get(index);
        }
        return element;
    }

    /**
     * @return the session id from the current driver (if available).
     */
    public String getSessionId() {
        String result = null;
        WebDriver d = driver();
        if (d instanceof RemoteWebDriver) {
            Object s = ((RemoteWebDriver) d).getSessionId();
            if (s != null) {
                result = s.toString();
            }
        }
        return result;
    }

    /**
     * @return true when current driver is connected to either a local or remote Internet Explorer.
     */
    public boolean connectedToInternetExplorer() {
        boolean result = false;
        WebDriver driver = driver();
        if (driver instanceof InternetExplorerDriver) {
            result = true;
        } else if (driver instanceof RemoteWebDriver) {
            result = checkRemoteBrowserName(driver, "internet explorer");
        }
        return result;
    }

    /**
     * @return true when current driver is connected to either a local or remote Safari.
     */
    public boolean connectedToSafari() {
        boolean result = false;
        WebDriver driver = driver();
        if (driver instanceof SafariDriver) {
            result = true;
        } else if (driver instanceof RemoteWebDriver) {
            result = checkRemoteBrowserName(driver, "safari");
        }
        return result;
    }

    protected boolean checkRemoteBrowserName(WebDriver driver, String expectedName) {
        RemoteWebDriver remoteWebDriver = (RemoteWebDriver) driver;
        String browserName = remoteWebDriver.getCapabilities().getBrowserName();
        return expectedName.equalsIgnoreCase(browserName);
    }

    /**
     * Allows direct access to WebDriver. If possible please use methods of this class to facilitate testing.
     * @return selenium web driver.
     */
    public WebDriver driver() {
        if (webDriver == null) {
            if (factory == null) {
                throw new StopTestException("Cannot use Selenium before a driver is started (for instance using SeleniumDriverSetup)");
            } else {
                factory.createDriver();
            }
        }
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
     * Executes condition until it returns a value other than null or false.
     * It does not forward StaleElementReferenceExceptions, but keeps waiting.
     * @param maxSecondsToWait number of seconds to wait at most.
     * @param condition condition to check.
     * @param <T> return type.
     * @return result of condition (if not null).
     * @throws TimeoutException when condition did not give a value to return after maxSecondsToWait.
     */
    public <T> T waitUntil(int maxSecondsToWait, ExpectedCondition<T> condition) {
        ExpectedCondition<T> cHandlingStale = getConditionIgnoringStaleElement(condition);
        FluentWait<WebDriver> wait = waitDriver().withTimeout(maxSecondsToWait, TimeUnit.SECONDS);
        return wait.until(cHandlingStale);
    }

    /**
     * Wraps the supplied condition so that StaleElementReferenceExceptions (and the Safari equivalent)
     * are to thrown by waitUntil(), but just mean: try again.
     * @param condition condition to wrap.
     * @param <T> retrun type of condition
     * @return wrapped condition.
     */
    public <T> ExpectedCondition<T> getConditionIgnoringStaleElement(final ExpectedCondition<T> condition) {
        return new ExpectedCondition<T>() {
            @Override
            public T apply(WebDriver webDriver) {
                try {
                    return condition.apply(webDriver);
                } catch (StaleElementReferenceException e) {
                    // try again
                    return null;
                } catch (WebDriverException e) {
                    String msg = e.getMessage();
                    if (msg != null
                            && (msg.contains("Element does not exist in cache")
                                // Safari stale element
                                || msg.contains("Error: element is not attached to the page document")
                                // Alternate Chrome stale element
                            )) {
                        return null;
                    } else {
                        throw e;
                    }
                }
            }
        };
    }

    /**
     * Finds element matching the By supplied.
     * @param context context to find element in.
     * @param atMostOne true indicates multiple matching elements (that have an id) should trigger an exception
     * @param by criteria.
     * @return element if found, null if none could be found.
     * @throws RuntimeException if atMostOne is true and multiple elements (having an id) match the by.
     */
    public WebElement findElement(SearchContext context, boolean atMostOne, By by) {
        WebElement element = null;
        List<WebElement> elements = context.findElements(by);
        if (elements.size() == 1) {
            element = elements.get(0);
        } else if (elements.size() > 1) {
            if (!atMostOne) {
                element = selectBestElement(elements);
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

    private WebElement selectBestElement(List<WebElement> elements) {
        // take the first displayed element without any elements on top of it,
        // if none: take first displayed
        // or if none are displayed: just take the first
        WebElement element = elements.get(0);
        WebElement firstDisplayed = null;
        WebElement firstOnTop = null;
        if (!element.isDisplayed() || !isOnTop(element)) {
            for (int i = 1; i < elements.size(); i++) {
                WebElement otherElement = elements.get(i);
                if (otherElement.isDisplayed()) {
                    if (firstDisplayed == null) {
                        firstDisplayed = otherElement;
                    }
                    if (isOnTop(otherElement)) {
                        firstOnTop = otherElement;
                        element = otherElement;
                        break;
                    }
                }
            }
            if (firstOnTop == null
                    && firstDisplayed != null
                    && !element.isDisplayed()) {
                // none displayed and on top
                // first was not displayed, but another was
                element = firstDisplayed;
            }
        }
        return element;
    }

    private boolean isOnTop(WebElement element) {
        WebElement e = (WebElement) executeJavascript(TOP_ELEMENT_AT, element);
        return element.equals(e);
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
     * Trigger scrolling of window to ensure element is in visible.
     * @param element element to scroll to.
     */
    public void scrollTo(WebElement element) {
        executeJavascript("arguments[0].scrollIntoView(true);", element);
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

    /**
     * Finds screenshot embedded in throwable, if any.
     * @param t exception to search in.
     * @return content of screenshot (if any is present), null otherwise.
     */
    public byte[] findScreenshot(Throwable t) {
        byte[] result = null;
        if (t != null) {
            if (t instanceof ScreenshotException) {
                String encodedScreenshot = ((ScreenshotException)t).getBase64EncodedScreenshot();
                result = new Base64Encoder().decode(encodedScreenshot);
            } else {
                result = findScreenshot(t.getCause());
            }
        }
        return result;
    }

    /**
     * Saves screenshot (as .png).
     * @param baseName name for file created (without extension),
     *                 if a file already exists with the supplied name an
     *                 '_index' will be added.
     * @return absolute path of file created.
     */
    public String writeScreenshot(String baseName, byte[] png) {
        return FileUtil.saveToFile(baseName, "png", png);
    }

    public String getResourceNameFromLocation() {
        String fileName = "pageSource";
        try {
            String location = driver().getCurrentUrl();
            URL u = new URL(location);
            String file = FilenameUtils.getName(u.getPath());
            file = file.replaceAll("^(.*?)(\\.html?)?$", "$1");
            if (!"".equals(file)) {
                fileName = file;
            }
        } catch (MalformedURLException e) {
            // ignore
        }
        return fileName;
    }

    /**
     * @return HTML content of current page.
     */
    public String getHtml() {
        String html;
        try {
            html = (String) executeJavascript(
                    "var node = document.doctype;\n" +
                            "var docType = '';\n" +
                            "if (node) {\n" +
                            "  docType = \"<!DOCTYPE \"\n" +
                            "+ node.name\n" +
                            "+ (node.publicId ? ' PUBLIC \"' + node.publicId + '\"' : '')\n" +
                            "+ (!node.publicId && node.systemId ? ' SYSTEM' : '') \n" +
                            "+ (node.systemId ? ' \"' + node.systemId + '\"' : '')\n" +
                            "+ '>'; }\n" +
                            "var html = document.documentElement.outerHTML " +
                            "|| '<html>' + document.documentElement.innerHTML + '</html>';\n" +
                            "return docType + html;"
            );
        } catch (RuntimeException e) {
            // unable to get via Javascript
            try {
                // this is very WebDriver implementation dependent, so we only use as fallback
                html = driver().getPageSource();
            } catch (Exception ex) {
                ex.printStackTrace();
                // throw original exception
                throw e;
            }
        }
        return html;
    }

    public PageSourceSaver getPageSourceSaver(String baseDir) {
        return new PageSourceSaver(baseDir, this);
    }

    /**
     * @return current window's size.
     */
    public Dimension getWindowSize() {
        return getWindow().getSize();
    }

    /**
     * Sets current window's size.
     * @param newWidth new width (in pixels)
     * @param newHeight new height (in pixels)
     */
    public void setWindowSize(int newWidth, int newHeight) {
        getWindow().setSize(new Dimension(newWidth, newHeight));
    }

    /**
     * Sets current window to maximum size.
     */

    public void setWindowSizeToMaximum() {
        getWindow().maximize();
    }

    /**
     * @return current browser window.
     */
    public WebDriver.Window getWindow() {
        return driver().manage().window();
    }

    public int getCurrentTabIndex(List<String> tabHandles) {
        try {
            String currentHandle = driver().getWindowHandle();
            return tabHandles.indexOf(currentHandle);
        } catch (NoSuchWindowException e) {
            return -1;
        }
    }

    public void goToTab(List<String> tabHandles, int indexToGoTo) {
        getTargetLocator().window(tabHandles.get(indexToGoTo));
        switchToDefaultContent();
        setCurrentContext(null);
    }

    public List<String> getTabHandles() {
        return new ArrayList<String>(driver().getWindowHandles());
    }

    /**
     * Activates main/top-level iframe (i.e. makes it the current frame).
     */
    public void switchToDefaultContent() {
        getTargetLocator().defaultContent();
        if (!currentIFramePath.isEmpty()) {
            setCurrentContext(null);
            currentIFramePath.clear();
        }
    }


    /**
     * Activates specified child frame of current iframe.
     * @param iframe frame to activate.
     */
    public void switchToFrame(WebElement iframe) {
        getTargetLocator().frame(iframe);
        setCurrentContext(null);
        currentIFramePath.add(iframe);
    }

    /**
     * Activates parent frame of current iframe.
     * Does nothing if when current frame is the main/top-level one.
     */
    public void switchToParentFrame() {
        if (!currentIFramePath.isEmpty()) {
            // copy path since substring creates a view, not a deep copy
            List<WebElement> newPath = currentIFramePath.subList(0, currentIFramePath.size() - 1);
            newPath = new ArrayList<WebElement>(newPath);
            // Safari and PhantomJs don't support switchTo.parentFrame, so we do this
            // it works for Phantom, but is VERY slow there (other browsers are slow but ok)
            switchToDefaultContent();
            for (WebElement iframe : newPath) {
                switchToFrame(iframe);
            }
        }
    }

    public <T> ExpectedCondition<T> conditionForAllFrames(ExpectedCondition<T> nested) {
        return new TryAllFramesConditionDecorator(this, nested);
    }

    /**
     * @return current alert, if one is present, null otherwise.
     */
    public Alert getAlert() {
        Alert alert = null;
        try {
            alert = getTargetLocator().alert();
        } catch (NoAlertPresentException e) {
            // just leave alert null
        }
        return alert;
    }

    private WebDriver.TargetLocator getTargetLocator() {
        return driver().switchTo();
    }

    /**
     * Gets current browser's cookie with supplied name.
     * @param cookieName name of cookie to return.
     * @return cookie, if present, null otherwise.
     */
    public Cookie getCookie(String cookieName) {
        return driver().manage().getCookieNamed(cookieName);
    }

    /**
     * @return current browser's cookies.
     */
    public Set<Cookie> getCookies() {
        return driver().manage().getCookies();
    }

    /**
     * Deletes all of the browser's cookies (for the current domain).
     */
    public void deleteAllCookies() {
        driver().manage().deleteAllCookies();
    }

    public void setDriverFactory(DriverFactory aFactory) {
        factory = aFactory;
    }

    /**
     * @param timeoutSeconds default number of seconds to wait before throwing timeout exceptions
     */
    public void setDefaultTimeoutSeconds(int timeoutSeconds) {
        defaultTimeoutSeconds = timeoutSeconds;
    }

    /**
     * @return default time for waiting (in seconds).
     */
    public int getDefaultTimeoutSeconds() {
        return defaultTimeoutSeconds;
    }

    /**
     * @return return current depth of (i)frames.
     */
    public int getCurrentFrameDepth() {
        return currentIFramePath.size();
    }

    /**
     * Store current frame depth in case of alert error
     * @param frameDepthOnAlert frames added searching in nested frames started, this is the number of levels that
     *                          should be removed after the alert is handled.
     */
    public void storeFrameDepthOnAlertError(int frameDepthOnAlert) {
        frameDepthOnLastAlertError = frameDepthOnAlert;
    }

    /**
     * Reactivate (i)frame that was active before we encountered an alert searching in nested (i)frames.
     */
    public void resetFrameDepthOnAlertError() {
        int depthOnLastAlertError = getFrameDepthOnLastAlertError();
        for (int i = 0; i < depthOnLastAlertError; i++) {
            switchToParentFrame();
            frameDepthOnLastAlertError--;
        }
    }

    /**
     * @return number of (i)frame levels that need to be removed to get back to right frame after encountering an alert
     *         in a nested (i)frame.
     */
    public int getFrameDepthOnLastAlertError() {
        return frameDepthOnLastAlertError;
    }

    public interface DriverFactory {
        void createDriver();
    }
}
