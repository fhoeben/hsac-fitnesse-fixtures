package nl.hsac.fitnesse.fixture.util.selenium;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import nl.hsac.fitnesse.fixture.slim.SlimFixtureException;
import nl.hsac.fitnesse.fixture.util.FileUtil;
import nl.hsac.fitnesse.fixture.util.selenium.by.ConstantBy;
import nl.hsac.fitnesse.fixture.util.selenium.by.CssBy;
import nl.hsac.fitnesse.fixture.util.selenium.by.ElementBy;
import nl.hsac.fitnesse.fixture.util.selenium.by.FirstElementBy;
import nl.hsac.fitnesse.fixture.util.selenium.by.InputBy;
import nl.hsac.fitnesse.fixture.util.selenium.by.IsDisplayedFilter;
import nl.hsac.fitnesse.fixture.util.selenium.by.IsInteractableFilter;
import nl.hsac.fitnesse.fixture.util.selenium.by.JavascriptBy;
import nl.hsac.fitnesse.fixture.util.selenium.by.LabelBy;
import nl.hsac.fitnesse.fixture.util.selenium.by.LinkBy;
import nl.hsac.fitnesse.fixture.util.selenium.by.TechnicalSelectorBy;
import nl.hsac.fitnesse.fixture.util.selenium.by.TextBy;
import nl.hsac.fitnesse.fixture.util.selenium.by.ToClickBy;
import nl.hsac.fitnesse.fixture.util.selenium.by.XPathBy;
import nl.hsac.fitnesse.fixture.util.selenium.by.relative.RelativeMethod;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Platform;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.ScreenshotException;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.locators.RelativeLocator;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import static nl.hsac.fitnesse.fixture.util.FirstNonNullHelper.firstNonNull;
import static org.openqa.selenium.support.locators.RelativeLocator.with;

/**
 * Helper to work with Selenium.
 */
public class SeleniumHelper<T extends WebElement> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SeleniumHelper.class);
    private static final String ELEMENT_ON_SCREEN_JS =
            "if (arguments[0].getBoundingClientRect) {\n" +
                    "var rect = arguments[0].getBoundingClientRect();\n" +
                    "return (\n" +
                    "  rect.top >= 0 &&\n" +
                    "  rect.left >= 0 &&\n" +
                    "  rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) &&\n" +
                    "  rect.right <= (window.innerWidth || document.documentElement.clientWidth));\n" +
                    "} else { return null; }";

    private static final String ALL_DIRECT_TEXT_CONTENT =
            "var element = arguments[0], text = '';\n" +
                    "for (var i = 0; i < element.childNodes.length; ++i) {\n" +
                    "  var node = element.childNodes[i];\n" +
                    "  if (node.nodeType == Node.TEXT_NODE" +
                    " && node.textContent.trim() != '')\n" +
                    "    text += node.textContent.trim();\n" +
                    "}\n" +
                    "return text;";
    private static final String DRAG_AND_DROP_SIM_JS_RESOURCE = "js/dragDropSim.js";
    private final static char NON_BREAKING_SPACE = 160;

    private final List<T> currentIFramePath = new ArrayList<>(4);
    private int frameDepthOnLastAlertError;
    private WebDriver webDriver;
    private WebDriverWait webDriverWait;
    private DevTools devTools;

    /**
     * Sets up webDriver to be used.
     *
     * @param aWebDriver     web driver to use.
     * @param defaultTimeout default timeout to wait, in seconds.
     */
    public void setWebDriver(WebDriver aWebDriver, int defaultTimeout) {
        if (webDriver != null && !webDriver.equals(aWebDriver)) {
            webDriver.quit();
        }
        webDriver = aWebDriver;

        if (webDriver == null) {
            webDriverWait = null;
        } else {
            webDriverWait = new WebDriverWait(webDriver, Duration.ofSeconds(defaultTimeout));
        }
    }

    /**
     * Shuts down selenium web driver.
     */
    public void close() {
        setWebDriver(null, 0);
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
     *
     * @param place identifier for element.
     * @return first interactable element found,
     * first element found if no interactable element could be found,
     * null if none could be found.
     */
    public T getElementToClick(String place) {
        return findByTechnicalSelectorOr(place, ToClickBy::heuristic);
    }

    /**
     * Finds link.
     *
     * @param place technical selector or (partial text on link).
     * @return first interactable link found,
     * first element found if no interactable link could be found,
     * null if none could be found.
     */
    public T getLink(String place) {
        return findByTechnicalSelectorOr(place, LinkBy::heuristic);
    }

    /**
     * Finds element to retrieve content from or enter content in, by searching in multiple locations.
     *
     * @param place identifier for element.
     * @return first interactable element found,
     * first element found if no interactable element could be found,
     * null if none could be found.
     */
    public T getElement(String place) {
        return findByTechnicalSelectorOr(place, ElementBy::heuristic);
    }

    /**
     * Finds an element by tag name, relative to a reference element, using the given RelativeMethod
     *
     * @param place          the place to find (i.e. input)
     * @param referencePlace the place to find as a reference for the tag to find
     * @param rMethod        the RelativeLocator method to use (above/blow/toLeftOf/toRightOf/near)
     * @return the element if found, or null if no element was found
     */
    public T getElementRelativeToReference(String place, String referencePlace, RelativeMethod rMethod) {
        try {
            T referenceElement = getElementToCheckVisibility(referencePlace);
            RelativeLocator.RelativeBy rBy = with(placeToBy(place));
            Method searchMethod = RelativeLocator.RelativeBy.class.getMethod(rMethod.toString(), WebElement.class);
            return findElement((RelativeLocator.RelativeBy) searchMethod.invoke(rBy, referenceElement));
        } catch (ReflectiveOperationException e) {
            throw new SlimFixtureException(true, "Error finding relative element", e);
        }
    }


    /**
     * Finds element to determine whether it is on screen, by searching in multiple locations.
     *
     * @param place identifier for element.
     * @return first interactable element found,
     * first element found if no interactable element could be found,
     * null if none could be found.
     */
    public T getElementToCheckVisibility(String place) {
        return findByTechnicalSelectorOr(place, () -> {
            T result = findElement(TextBy.partial(place));
            if (!IsDisplayedFilter.mayPass(result)) {
                result = findElement(ToClickBy.heuristic(place));
            }
            return result;
        });
    }

    public T findByTechnicalSelectorOr(String place, Function<String, By> byFunction) {
        By by = placeToBy(place);
        if (by == null) {
            by = byFunction.apply(place);
        }
        return findElement(by);
    }

    public final T findByTechnicalSelectorOr(String possibleTechnicalSelector, Supplier<? extends T>... suppliers) {
        T element;
        By by = placeToBy(possibleTechnicalSelector);
        if (by != null) {
            element = findElement(by);
        } else {
            if (suppliers.length == 1) {
                element = suppliers[0].get();
            } else {
                element = firstNonNull((Supplier<T>[]) suppliers);
            }
        }
        return element;
    }

    public By placeToBy(String place) {
        return TechnicalSelectorBy.forPlace(place);
    }

    /**
     * @param element element to check.
     * @return whether the element is displayed and enabled.
     */
    public boolean isInteractable(WebElement element) {
        return IsInteractableFilter.mayPass(element);
    }

    public T getLabelledElement(T label) {
        return doInCurrentContext(c -> (T) LabelBy.getLabelledElement(c, label));
    }

    public T getNestedElementForValue(T parent) {
        return (T) ConstantBy.nestedElementForValue().findElement(parent);
    }

    /**
     * Determines number displayed for item in ordered list.
     *
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
     *
     * @param element select element to find options for.
     * @return text per option.
     */
    public ArrayList<String> getAvailableOptions(WebElement element) {
        ArrayList<String> result = null;
        if (isInteractable(element)
                && "select".equalsIgnoreCase(element.getTagName())) {
            result = new ArrayList<>();
            List<WebElement> options = element.findElements(By.tagName("option"));
            for (WebElement option : options) {
                if (option.isEnabled()) {
                    result.add(option.getText());
                }
            }
        }
        return result;
    }

    public int countVisibleOccurrences(String text, boolean checkOnScreen) {
        By findAllTexts = TextBy.partial(text);
        List<T> texts = findElements(findAllTexts);
        int result = countDisplayedElements(texts, text, checkOnScreen);

        By findAllInputs = InputBy.partialNormalizedValue(text);
        List<T> inputs = findElements(findAllInputs);
        result = result + countDisplayedValues(inputs, text, checkOnScreen);

        return result;
    }

    private int countDisplayedElements(List<T> elements, String textToFind, boolean checkOnScreen) {
        int result = 0;
        for (WebElement element : elements) {
            if (checkVisible(element, checkOnScreen)) {
                if ("option".equalsIgnoreCase(element.getTagName())) {
                    T select = (T) element.findElement(By.xpath("./ancestor::select"));
                    Select s = new Select(select);
                    if (s.isMultiple()) {
                        // for multi-select we count all options as visible
                        int occurrencesInText = getOccurrencesInText(element, textToFind);
                        result += occurrencesInText;
                    } else {
                        // for drop down we only count only selected option
                        T selected = (T) s.getFirstSelectedOption();
                        if (element.equals(selected)) {
                            int occurrencesInText = getOccurrencesInText(element, textToFind);
                            result += occurrencesInText;
                        }
                    }
                } else {
                    int occurrencesInText = getOccurrencesInText(element, textToFind);
                    result += occurrencesInText;
                }
            }
        }
        return result;
    }

    private int getOccurrencesInText(WebElement element, String textToFind) {
        String elementText = getAllDirectText(element);
        return countOccurrences(elementText, textToFind);
    }

    private int countDisplayedValues(List<T> elements, String textToFind, boolean checkOnScreen) {
        int result = 0;
        for (WebElement element : elements) {
            if (checkVisible(element, checkOnScreen)) {
                String value = element.getAttribute("value");
                int occurrencesInValue = countOccurrences(value, textToFind);
                result += occurrencesInValue;
            }
        }
        return result;
    }

    public boolean checkVisible(WebElement element, boolean checkOnScreen) {
        boolean result = false;
        if (IsDisplayedFilter.mayPass(element)) {
            if (checkOnScreen) {
                result = isElementOnScreen(element);
            } else {
                result = true;
            }
        }
        return result;
    }

    private int countOccurrences(String value, String textToFind) {
        String normalizedValue = XPathBy.getNormalizedText(value);
        return StringUtils.countMatches(normalizedValue, textToFind);
    }

    /**
     * Gets element's text content.
     *
     * @param element element to get text() of.
     * @return text, without trailing whitespace and with &nbsp; as normal spaces.
     */
    public String getText(WebElement element) {
        String text = element.getText();
        if (text != null) {
            // Safari driver does not return &nbsp; as normal spacce, while others do
            text = text.replace(NON_BREAKING_SPACE, ' ');
            // Safari driver does not return trim, while others do
            text = text.trim();
        }
        return text;
    }

    /**
     * Gets the entire text of element, without the text elements of its children (which a normal element.getText()
     * does include).
     *
     * @param element element to get text from.
     * @return all text in the element.
     */
    public String getAllDirectText(WebElement element) {
        return (String) executeJavascript(ALL_DIRECT_TEXT_CONTENT, element);
    }

    /**
     * Sets value of input field of type 'date'.
     * Entering a value using sendKeys requires you to know what format the browser's OS is using for dates,
     * this method prevents that problem by setting the input's value directly.
     *
     * @param element id or name of input field to set.
     * @param value   value to set, must be in format yyyy-mm-dd.
     */
    public void fillDateInput(WebElement element, String value) {
        executeJavascript("arguments[0].value = arguments[1]", element, value);
    }

    /**
     * Sets value of hidden input field.
     *
     * @param idOrName id or name of input field to set.
     * @param value    value to set.
     * @return whether input field was found.
     */
    public boolean setHiddenInputValue(String idOrName, String value) {
        T element = findElement(By.id(idOrName));
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
     *
     * @param statementPattern javascript to run, possibly with placeholders to be replaced.
     * @param parameters       placeholder values that should be replaced before executing the script.
     * @return return value from statement.
     * @link http://selenium.googlecode.com/git/docs/api/java/org/openqa/selenium/JavascriptExecutor.html#executeScript(java.lang.String,%20java.lang.Object...)
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
        JavascriptExecutor jse = (JavascriptExecutor) driver();
        Object result = JavascriptHelper.executeScript(jse, script, parameters);
        return result;
    }

    /**
     * Executes Javascript in browser and then waits for 'callback' to be invoked.
     * If statementPattern should reference the magic (function) variable 'callback' which should be
     * called to provide this method's result.
     * If the statementPattern contains the magic variable 'arguments'
     * the parameters will also be passed to the statement. In the latter case the parameters
     * must be a number, a boolean, a String, WebElement, or a List of any combination of the above.
     *
     * @param statementPattern javascript to run, possibly with placeholders to be replaced.
     * @param parameters       placeholder values that should be replaced before executing the script.
     * @return return value from statement.
     * @link http://selenium.googlecode.com/git/docs/api/java/org/openqa/selenium/JavascriptExecutor.html#executeAsyncScript(java.lang.String,%20java.lang.Object...)
     */
    public Object waitForJavascriptCallback(String statementPattern, Object... parameters) {
        JavascriptExecutor jse = (JavascriptExecutor) driver();
        Object result = JavascriptHelper.waitForJavascriptCallback(jse, statementPattern, parameters);
        return result;
    }

    /**
     * Creates By based on CSS selector, supporting placeholder replacement.
     *
     * @param pattern    basic CSS selector, possibly with placeholders.
     * @param parameters values for placeholders.
     * @return ByCssSelector.
     */
    public By byCss(String pattern, String... parameters) {
        return new CssBy(pattern, parameters);
    }

    /**
     * Creates By based on xPath, supporting placeholder replacement.
     * It also supports the fictional 'normalized()' function that does whitespace normalization, that also
     * considers a '&nbsp;' whitespace.
     *
     * @param pattern    basic XPATH, possibly with placeholders.
     * @param parameters values for placeholders.
     * @return ByXPath.
     */
    public By byXpath(String pattern, String... parameters) {
        return new XPathBy(pattern, parameters);
    }

    public By byJavascript(String pattern, Object... arguments) {
        return new JavascriptBy(pattern, arguments);
    }

    /**
     * Checks whether element is in browser's viewport.
     *
     * @param element element to check
     * @return true if element is in browser's viewport, null if we could not determine whether it was in viewport.
     */
    public Boolean isElementOnScreen(WebElement element) {
        return (Boolean) executeJavascript(ELEMENT_ON_SCREEN_JS, element);
    }

    /**
     * Sets how long to wait before deciding an element does not exists.
     *
     * @param implicitWait time in milliseconds to wait.
     */
    public void setImplicitlyWait(int implicitWait) {
        try {
            driver().manage().timeouts().implicitlyWait(Duration.ofMillis(implicitWait));
        } catch (Exception e) {
            // https://code.google.com/p/selenium/issues/detail?id=6015
            LOGGER.error("Unable to set implicit timeout (known issue for Safari): " + e.getMessage());
        }
    }

    /**
     * Sets how long to wait when executing asynchronous script calls.
     *
     * @param scriptTimeout time in milliseconds to wait.
     */
    public void setScriptWait(int scriptTimeout) {
        try {
            driver().manage().timeouts().scriptTimeout(Duration.ofMillis(scriptTimeout));
        } catch (Exception e) {
            // https://code.google.com/p/selenium/issues/detail?id=6015
            LOGGER.error("Unable to set script timeout (known issue for Safari): " + e.getMessage());
        }
    }

    /**
     * Sets how long to wait on opening a page.
     *
     * @param pageLoadWait time in milliseconds to wait.
     */
    public void setPageLoadWait(int pageLoadWait) {
        try {
            driver().manage().timeouts().pageLoadTimeout(Duration.ofMillis(pageLoadWait));
        } catch (Exception e) {
            // https://code.google.com/p/selenium/issues/detail?id=6015
            LOGGER.error("Unable to set page load timeout (known issue for Safari): " + e.getMessage());
        }
    }

    /**
     * Simulates placing the mouse over the supplied element.
     *
     * @param element element to place mouse over.
     */
    public void hoverOver(WebElement element) {
        getActions().moveToElement(element).perform();
    }

    /**
     * Simulates placing the mouse at offset from the supplied element center.
     *
     * @param element element to place mouse over.
     * @param xOffset horizontal integer offset from center.
     * @param yOffset vertical integer offset from center.
     */
    public Actions moveToElement(WebElement element, Integer xOffset, Integer yOffset) {
        return getActions().moveToElement(element, xOffset, yOffset);
    }

    /**
     * Simulates clicking at offset from center place on the supplied element.
     *
     * @param element element to click on with ofset.
     * @param xOffset horizontal integer offset from center.
     * @param yOffset vertical integer offset from center.
     */
    public void clickAtOffsetXY(WebElement element, Integer xOffset, Integer yOffset) {
        moveToElement(element, xOffset, yOffset).click().build().perform();
    }

    /**
     * Simulates right clicking at offset from center place on the supplied element.
     *
     * @param element element to click on with ofset.
     * @param xOffset horizontal integer offset from given element center.
     * @param yOffset vertical integer offset from given element center.
     */
    public void rightClickAtOffsetXY(WebElement element, Integer xOffset, Integer yOffset) {
        moveToElement(element, xOffset, yOffset).contextClick().build().perform();
    }

    /**
     * Simulates double clicking at offset from center place on the supplied element.
     *
     * @param element element to click on with ofset.
     * @param xOffset horizontal integer offset from given element center.
     * @param yOffset vertical integer offset from given element center.
     */
    public void doubleClickAtOffsetXY(WebElement element, Integer xOffset, Integer yOffset) {
        moveToElement(element, xOffset, yOffset).doubleClick().build().perform();
    }

    /**
     * Simulates a drag of element to destination offsets calculated from element center.
     *
     * @param element element to drag and drop.
     * @param xOffset horizontal integer offset destination for dropping (calculated from given element center).
     * @param yOffset vertical integer offset destination for dropping (calculated from given element center).
     */
    public void dragAndDropToOffsetXY(WebElement element, Integer xOffset, Integer yOffset) {
        getActions().dragAndDropBy(element, xOffset, yOffset).build().perform();
    }

    /**
     * Simulates a drag of element to destination offsets calculated from element center.
     * Performs a slight user defined diagonal movement of element to avoid limitations of "distance"
     * More info: https://github.com/clauderic/react-sortable-hoc
     *
     * @param element  element to drag and drop.
     * @param distance integer horizontal and vertical offsets to move element (calculated from given element center).
     * @param xOffset  horizontal integer offset destination for dropping (calculated from given element center).
     * @param yOffset  vertical integer offset destination for dropping (calculated from given element center).
     */
    public void dragWithDistanceAndDropToOffsetXY(WebElement element, int distance, Integer xOffset, Integer yOffset) {
        getActions().clickAndHold(element)
                .moveByOffset(distance, distance)
                .moveByOffset(xOffset - distance, yOffset - distance)
                .release().perform();
    }

    /**
     * Simulates a drag from source react element and drop to target element
     * Performs a slight user defined delay before drag to avoid limitations of "pressDelay"
     * More info: https://github.com/clauderic/react-sortable-hoc
     *
     * @param source  element to start the drag
     * @param delay   long to define how much to delay in millis after clickAndHold
     * @param xOffset horizontal integer offset destination for dropping (calculated from given element center).
     * @param yOffset vertical integer offset destination for dropping (calculated from given element center).
     */
    public void dragWithDelayAndDropToOffsetXY(WebElement source, long delay, Integer xOffset, Integer yOffset) {
        getActions().clickAndHold(source)
                .pause(delay)
                .moveByOffset(xOffset, yOffset)
                .release().perform();
    }

    /**
     * Simulates double clicking on the supplied element
     *
     * @param element element to double click on
     */
    public void doubleClick(WebElement element) {
        getActions().doubleClick(element).perform();
    }

    /**
     * Simulates right clicking (i.e. context clicking) on the supplied element
     *
     * @param element element to right click on
     */
    public void rightClick(WebElement element) {
        getActions().contextClick(element).perform();
    }

    /**
     * Simulates clicking with the supplied key pressed on the supplied element.
     * Key will be released after click.
     *
     * @param element element to click on
     */
    public void clickWithKeyDown(WebElement element, CharSequence key) {
        getActions().keyDown(key).click(element).keyUp(key).perform();
    }

    /**
     * Simulates a drag from source element and drop to target element
     *
     * @param source element to start the drag
     * @param target element to end the drag
     */
    public void dragAndDrop(WebElement source, WebElement target) {
        getActions().dragAndDrop(source, target).perform();
    }

    /**
     * Simulates a drag from source element and drop to target element
     * Performs a slight user defined movement of element to avoid limitations of "distance"
     * More info: https://github.com/clauderic/react-sortable-hoc
     *
     * @param source   element to start the drag
     * @param target   element to end the drag
     * @param distance integer offset for horizontal and vertical element move (calculated from given element center).
     */
    public void dragWithDistanceAndDrop(WebElement source, int distance, WebElement target) {
        getActions().clickAndHold(source)
                .moveByOffset(distance, distance)
                .moveToElement(target)
                .release().perform();
    }

    /**
     * Simulates a drag from source element and drop to target element
     * Performs a slight user defined delay before drag to avoid limitations of "pressDelay"
     * More info: https://github.com/clauderic/react-sortable-hoc
     *
     * @param source element to start the drag
     * @param delay  long to define how much to delay in millis after clickAndHold
     * @param target element to end the drag
     */
    public void dragWithDelayAndDrop(WebElement source, long delay, WebElement target) {
        getActions().clickAndHold(source)
                .pause(delay)
                .moveToElement(target)
                .release().perform();
    }

    /**
     * Simulates a drag from source element and drop to target element. HTML5 draggable-compatible
     * Workaround for https://github.com/seleniumhq/selenium-google-code-issue-archive/issues/3604
     * Uses https://github.com/Photonios/JS-DragAndDrop-Simulator for maximum compatibility
     *
     * @param source element to start the drag
     * @param target element to end the drag
     * @throws IOException when the simulator javascript is not found on the classpath
     */
    public void html5DragAndDrop(WebElement source, WebElement target) throws IOException {
        URL url = Resources.getResource(DRAG_AND_DROP_SIM_JS_RESOURCE);
        String js = Resources.toString(url, Charsets.UTF_8);
        executeJavascript(js + " DndSimulator.simulate(arguments[0], arguments[1]);", source, target);
    }

    public Actions getActions() {
        return new Actions(driver());
    }

    /**
     * @return currently active element.
     */
    public T getActiveElement() {
        return (T) getTargetLocator().activeElement();
    }

    /**
     * Simulates 'select all' (e.g. Ctrl+A on Windows) on the active element.
     *
     * @return whether an active element was found.
     */
    public boolean selectAll() {
        boolean result = false;
        WebElement element = getActiveElement();
        if (element != null) {
            if (connectedToMac()) {
                executeJavascript("arguments[0].select()", element);
            } else {
                element.sendKeys(Keys.CONTROL, "a");
            }
            result = true;
        }
        return result;
    }

    /**
     * Simulates 'copy' (e.g. Ctrl+C on Windows) on the active element, copying the current selection to the clipboard.
     *
     * @return whether an active element was found.
     */
    public boolean copy() {
        boolean result = false;
        WebElement element = getActiveElement();
        if (element != null) {
            if (connectedToMac()) {
                element.sendKeys(Keys.CONTROL, Keys.INSERT);
            } else {
                element.sendKeys(Keys.CONTROL, "c");
            }
            result = true;
        }
        return result;
    }

    /**
     * Simulates 'cut' (e.g. Ctrl+X on Windows) on the active element, copying the current selection to the clipboard
     * and removing that selection.
     *
     * @return whether an active element was found.
     */
    public boolean cut() {
        boolean result = false;
        WebElement element = getActiveElement();
        if (element != null) {
            if (connectedToMac()) {
                element.sendKeys(Keys.chord(Keys.CONTROL, Keys.INSERT), Keys.BACK_SPACE);
            } else {
                element.sendKeys(Keys.CONTROL, "x");
            }
            result = true;
        }
        return result;
    }

    /**
     * Simulates 'paste' (e.g. Ctrl+V on Windows) on the active element, copying the current clipboard
     * content to the currently active element.
     *
     * @return whether an active element was found.
     */
    public boolean paste() {
        boolean result = false;
        WebElement element = getActiveElement();
        if (element != null) {
            if (connectedToMac()) {
                element.sendKeys(Keys.SHIFT, Keys.INSERT);
            } else {
                element.sendKeys(Keys.CONTROL, "v");
            }
            result = true;
        }
        return result;
    }

    /**
     * @return text currently selected in browser, or empty string if no text is selected.
     */
    public String getSelectionText() {
        return (String) executeJavascript("return window.getSelection? window.getSelection().toString() : \"\"");
    }

    /**
     * Finds element using xPath, supporting placeholder replacement.
     *
     * @param pattern    basic XPATH, possibly with placeholders.
     * @param parameters values for placeholders.
     * @return element if found, null if none could be found.
     */
    public T findByXPath(String pattern, String... parameters) {
        By by = byXpath(pattern, parameters);
        return findElement(by);
    }

    /**
     * Finds element matching the By supplied.
     *
     * @param by criteria.
     * @return element if found, null if none could be found.
     */
    public T findElement(By by) {
        return doInCurrentContext(c -> findElement(c, by));
    }

    /**
     * Finds element matching the By supplied.
     *
     * @param by criteria.
     * @return element if found, null if none could be found.
     */
    public List<T> findElements(By by) {
        return doInCurrentContext(c -> (List<T>) c.findElements(by));
    }

    private SearchContext currentContext;
    private boolean currentContextIsStale = false;

    public void setCurrentContext(SearchContext newContext) {
        currentContext = newContext;
        currentContextIsStale = false;
    }

    public SearchContext getCurrentContext() {
        SearchContext result;
        if (currentContext == null) {
            result = driver();
        } else {
            if (currentContextIsStale) {
                throw new StaleContextException(currentContext);
            }
            result = currentContext;
        }
        return result;
    }

    /**
     * Perform action/supplier in current context.
     *
     * @param function function to perform.
     * @param <R>      type of result.
     * @return function result.
     * @throws StaleContextException if function threw stale element exception (i.e. current context could not be used)
     */
    public <R> R doInCurrentContext(Function<SearchContext, ? extends R> function) {
        try {
            return function.apply(getCurrentContext());
        } catch (WebDriverException e) {
            if (isStaleElementException(e)) {
                // current context was no good to search in
                currentContextIsStale = true;
                // by getting the context we trigger explicit exception
                getCurrentContext();
            }
            throw e;
        }
    }

    /**
     * Perform action/supplier in context.
     *
     * @param context context to perfom action in.
     * @param action  action to perform.
     * @param <R>     type of action result.
     * @return action result.
     */
    public <R> R doInContext(SearchContext context, Supplier<R> action) {
        R result;
        if (context == null) {
            result = action.get();
        } else {
            // store current context (without triggering exception if it was stale)
            SearchContext currentSearchContext = currentContext;
            boolean contextIsStale = currentContextIsStale;

            setCurrentContext(context);
            try {
                result = action.get();
            } finally {
                // make original current context active again
                currentContext = currentSearchContext;
                currentContextIsStale = contextIsStale;
            }
        }
        return result;
    }

    /**
     * Finds the nth element matching the By supplied.
     *
     * @param by    criteria.
     * @param index (zero based) matching element to return.
     * @return element if found, null if none could be found.
     */
    public T findElement(By by, int index) {
        T element = null;
        List<T> elements = findElements(by);
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
     * @return Keys#COMMAND if the browser is running on a Mac, or Keys#CONTROL otherwise.
     */
    public Keys getControlOrCommand() {
        return connectedToMac() ? Keys.COMMAND : Keys.CONTROL;
    }

    /**
     * @return whether current driver connects to browser on a Mac
     */
    public boolean connectedToMac() {
        boolean isMac;
        WebDriver driver = driver();
        if (driver instanceof RemoteWebDriver) {
            RemoteWebDriver remoteWebDriver = (RemoteWebDriver) driver;
            Platform platform = remoteWebDriver.getCapabilities().getPlatformName();
            isMac = Platform.MAC == platform || Platform.MAC == platform.family();
        } else {
            isMac = SystemUtils.IS_OS_MAC;
        }
        return isMac;
    }

    /**
     * Allows direct access to WebDriver. If possible please use methods of this class to facilitate testing.
     *
     * @return selenium web driver.
     */
    public WebDriver driver() {
        return webDriver;
    }

    /**
     * return an augmented driver if it is not screenshot-capable (i.e. not a RemoteWebDriver)
     *
     * @return selenium web driver with screenshot capabilities
     */
    protected TakesScreenshot screenshotCapableDriver() {
        WebDriver d = driver();

        if (!(d instanceof TakesScreenshot)) {
            d = new Augmenter().augment(d);
        }
        if (d instanceof TakesScreenshot) {
            return (TakesScreenshot) d;
        }
        return null;
    }

    /**
     * Allows clients to wait until a certain condition is true.
     *
     * @return wait using the driver in this helper.
     */
    public WebDriverWait waitDriver() {
        return webDriverWait;
    }

    /**
     * Executes condition until it returns a value other than null or false.
     * It does not forward StaleElementReferenceExceptions, but keeps waiting.
     *
     * @param maxSecondsToWait number of seconds to wait at most.
     * @param condition        condition to check.
     * @param <T>              return type.
     * @return result of condition (if not null).
     * @throws TimeoutException when condition did not give a value to return after maxSecondsToWait.
     */
    public <T> T waitUntil(int maxSecondsToWait, ExpectedCondition<T> condition) {
        ExpectedCondition<T> cHandlingStale = getConditionIgnoringStaleElement(condition);
        FluentWait<WebDriver> wait = waitDriver().withTimeout(Duration.ofSeconds(maxSecondsToWait));
        return wait.until(cHandlingStale);
    }

    /**
     * Wraps the supplied condition so that StaleElementReferenceExceptions (and the Safari equivalent)
     * are to thrown by waitUntil(), but just mean: try again.
     *
     * @param condition condition to wrap.
     * @param <T>       retrun type of condition
     * @return wrapped condition.
     */
    public <T> ExpectedCondition<T> getConditionIgnoringStaleElement(final ExpectedCondition<T> condition) {
        return d -> {
            try {
                return condition.apply(webDriver);
            } catch (WebDriverException e) {
                if (isStaleElementException(e)) {
                    return null;
                } else {
                    throw e;
                }
            }
        };
    }

    /**
     * Check whether exception indicates a 'stale element', not all drivers throw the exception one would expect...
     *
     * @param e exception caught
     * @return true if exception indicated the element is no longer part of the page in the browser.
     */
    public boolean isStaleElementException(WebDriverException e) {
        boolean result = false;
        if (e instanceof StaleElementReferenceException) {
            result = true;
        } else {
            String msg = e.getMessage();
            if (msg != null) {
                result = msg.contains("Element does not exist in cache") // Safari stale element
                        || msg.contains("unknown error: unhandled inspector error: {\"code\":-32000,\"message\":\"Cannot find context with specified id\"}") // chrome error
                        || msg.contains("unknown error: unhandled inspector error: {\"code\":-32000,\"message\":\"Node with given id does not belong to the document\"}") // new chrome stale element error
                        || msg.contains("Error: element is not attached to the page document") // Alternate Chrome stale element
                        || msg.contains("uniqueContextId not found") // Chrome error
                        || msg.contains("can't access dead object"); // Firefox stale element
            }
        }
        return result;
    }

    /**
     * Check whether exception indicates connection with webdriver is lost.
     *
     * @param e exception caught
     * @return true if exception indicated we can no longer communicate with webdriver.
     */
    public boolean exceptionIndicatesConnectionLost(WebDriverException e) {
        boolean result = e.getCause() instanceof SocketException;
        if (!result && e.getMessage() != null) {
            result = e.getMessage().contains("java.net.SocketException")
                    || e.getMessage().contains("java.net.ConnectException");
        }
        return result;
    }


    /**
     * Finds element matching the By supplied.
     *
     * @param context context to find element in.
     * @param by      criteria.
     * @return element if found, null if none could be found.
     */
    public T findElement(SearchContext context, By by) {
        return FirstElementBy.getWebElement(by, context);
    }

    /**
     * Trigger scrolling of window to ensure element is in visible.
     *
     * @param element  element to scroll to.
     * @param toCenter boolean value indicating wether to center the element in the viewport or use the automatic setting
     */
    public void scrollTo(WebElement element, boolean toCenter) {
        String scrollIntoViewArgs = toCenter ? "{behavior: \"auto\", block: \"center\", inline: \"center\"}" : "true";
        executeJavascript("arguments[0].scrollIntoView(" + scrollIntoViewArgs + ");", element);
    }

    /**
     * Takes screenshot of current page (as .png).
     *
     * @param baseName name for file created (without extension),
     *                 if a file already exists with the supplied name an
     *                 '_index' will be added.
     * @return absolute path of file created.
     */
    public String takeScreenshot(String baseName) {
        String result = null;

        TakesScreenshot ts = screenshotCapableDriver();
        if (ts != null) {
            byte[] png = ts.getScreenshotAs(OutputType.BYTES);
            result = writeScreenshot(baseName, png);
        }
        return result;
    }

    /**
     * Takes screenshot of a specific element on the page (as .png).
     *
     * @param baseName name for file created (without extension),
     *                 if a file already exists with the supplied name an
     *                 '_index' will be added.
     * @param element  the webelement to limit the image to.
     * @return absolute path of file created.
     */
    public String takeElementScreenshot(String baseName, WebElement element) {
        String result = null;

        TakesScreenshot ts = screenshotCapableDriver();
        if (ts != null) {
            byte[] png = element.getScreenshotAs(OutputType.BYTES);
            result = writeScreenshot(baseName, png);
        }
        return result;
    }

    /**
     * Finds screenshot embedded in throwable, if any.
     *
     * @param t exception to search in.
     * @return content of screenshot (if any is present), null otherwise.
     */
    public byte[] findScreenshot(Throwable t) {
        byte[] result = null;
        if (t != null) {
            if (t instanceof ScreenshotException) {
                String encodedScreenshot = ((ScreenshotException) t).getBase64EncodedScreenshot();
                result = Base64.getDecoder().decode(encodedScreenshot);
            } else {
                result = findScreenshot(t.getCause());
            }
        }
        return result;
    }

    /**
     * Saves screenshot (as .png).
     *
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
     *
     * @param newWidth  new width (in pixels)
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
        return new ArrayList<>(driver().getWindowHandles());
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
     *
     * @param iframe frame to activate.
     */
    public void switchToFrame(T iframe) {
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
            List<T> newPath = currentIFramePath.subList(0, currentIFramePath.size() - 1);
            newPath = new ArrayList<T>(newPath);
            // Safari and PhantomJs don't support switchTo.parentFrame, so we do this
            // it works for Phantom, but is VERY slow there (other browsers are slow but ok)
            switchToDefaultContent();
            for (T iframe : newPath) {
                switchToFrame(iframe);
            }
        }
    }

    public <T> ExpectedCondition<T> conditionForAllFrames(ExpectedCondition<T> nested) {
        return new TryAllFramesConditionDecorator<>(this, nested);
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
     *
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

    /**
     * @return return current depth of (i)frames.
     */
    public int getCurrentFrameDepth() {
        return currentIFramePath.size();
    }

    /**
     * Store current frame depth in case of alert error
     *
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
     * in a nested (i)frame.
     */
    public int getFrameDepthOnLastAlertError() {
        return frameDepthOnLastAlertError;
    }
}
