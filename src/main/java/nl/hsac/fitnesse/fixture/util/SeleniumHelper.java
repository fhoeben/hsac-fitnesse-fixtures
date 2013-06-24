package nl.hsac.fitnesse.fixture.util;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Helper to work with Selenium.
 */
public class SeleniumHelper {

    private static WebDriver WEB_DRIVER = null;
    private static boolean SHUTDOWN_HOOK_ENABLED = false;

    private static WebDriver getWebDriver() {
        if(WEB_DRIVER == null) {
            if(!SHUTDOWN_HOOK_ENABLED) {
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    public void run() {
                        closeInstance();
                    }
                });
                SHUTDOWN_HOOK_ENABLED = true;
            }
            WEB_DRIVER = new FirefoxDriver();
        }

        return WEB_DRIVER;
    }

    private static void closeInstance() {
        if(WEB_DRIVER != null) {
            WEB_DRIVER.quit();
            WEB_DRIVER = null;
        }
    }

    /**
     * Shuts down selenium web driver.
     */
    public void close() {
        closeInstance();
    }

    /**
     * @return current page title.
     */
    public String getPageTitle() {
        return getWebDriver().getTitle();
    }

    /**
     * @return Selenium's navigation.
     */
    public WebDriver.Navigation navigate() {
        return getWebDriver().navigate();
    }

    /**
     * Finds element, by searching in multiple locations.
     * @param place identifier for element.
     * @return first element found, null if none could be found.
     */
    public WebElement getElement(String place) {
        WebElement element = null;
        if (element == null) {
            element = getElementByLabel(place);
        }
        if (element == null) {
            element = findElement(byXpath("//input[@value='%s']", place));
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
            element = getElementByPartialLabel(place);
        }
        if (element == null) {
            element = findElement(By.partialLinkText(place));
        }
        return element;
    }

    private WebElement getElementByLabel(String labelText) {
        return getElementByLabel(labelText, "//label[text()='%s']", "//*[@aria-label='%s']");
    }

    private WebElement getElementByPartialLabel(String labelText) {
        return getElementByLabel(labelText, "//label[contains(text(), '%s')]", "//*[contains(@aria-label, '%s')]");
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
     * Creates By based on xPath, supporting placeholder replacement.
     * @param pattern basic XPATH, possibly with placeholders.
     * @param parameters values for placeholders.
     * @return ByXPath.
     */
    public By byXpath(String pattern, String... parameters) {
        Object[] escapedParams = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            escapedParams[i] = xpathEscape(parameters[i]);
        }
        String xpath = String.format(pattern, escapedParams);
        return By.xpath(xpath);
    }

    private String xpathEscape(String value) {
        return value.replace("\"", "&quot;")
                    .replace("'", "&apos;");
    }

    /**
     * Sets how long to wait before deciding an element does not exists.
     * @param implicitWait time in milliseconds to wait.
     */
    public void setImplicitlyWait(int implicitWait) {
        getWebDriver().manage().timeouts().implicitlyWait(implicitWait, TimeUnit.MILLISECONDS);
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
        return findElement(getWebDriver(), atMostOne, by);
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
            elements = elementsWithId(elements);
            if (elements.size() == 1 || !atMostOne) {
                element = elements.get(0);
            } else {
                throw new RuntimeException("Multiple elements with id found for: " + by
                                            + ":\n" + elementsAsString(elements));
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

}
