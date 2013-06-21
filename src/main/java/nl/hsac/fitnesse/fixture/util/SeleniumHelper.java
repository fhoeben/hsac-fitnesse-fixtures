package nl.hsac.fitnesse.fixture.util;

import org.openqa.selenium.By;
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
    /** Timeout in milliseconds to wait for element to appear */
    public static int IMPLICIT_WAIT = 15000;

    public static WebDriver getWebDriver() {
        if(WEB_DRIVER == null) {
            if(!SHUTDOWN_HOOK_ENABLED) {
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    public void run() {
                        close();
                    }
                });
                SHUTDOWN_HOOK_ENABLED = true;
            }
			/*
			 * Vervang dit eventueel door een browser naar keuze.
			 */
            WEB_DRIVER = new FirefoxDriver();
            setImplicitWait(IMPLICIT_WAIT);
        }

        return WEB_DRIVER;
    }

    private static void setImplicitWait(int implicitWait) {
        WEB_DRIVER.manage().timeouts().implicitlyWait(implicitWait, TimeUnit.MILLISECONDS);
    }

    public static void close() {
        if(WEB_DRIVER != null) {
            WEB_DRIVER.quit();
            WEB_DRIVER = null;
        }
    }

    public WebElement getElement(String place) {
        WebElement element = null;
        setImplicitlyWait(100);
        String xpathPlace = place.replace("\"", "&quot;")
                                 .replace("'", "&apos;").trim();
        if (element == null) {
            WebElement label = findElement(By.xpath("//label[text()='" + xpathPlace + "']"));
            if (label != null) {
                String forAttr = label.getAttribute("for");
                element = findElement(By.id(forAttr));
            }
        }
        if (element == null) {
            element = findElement(By.xpath("//*[@aria-label='" + xpathPlace + "']"));
        }
        if (element == null) {
            element = findElement(By.name(place));
        }
        if (element == null) {
            element = findElement(By.id(place));
        }
        setImplicitlyWait(IMPLICIT_WAIT);
        return element;
    }

    public void setImplicitlyWait(int implicitWait) {
        setImplicitWait(implicitWait);
    }

    public WebElement findElement(By by) {
        WebElement element = null;
        List<WebElement> elements = getWebDriver().findElements(by);
        if (elements.size() == 1) {
            element = elements.get(0);
        } else if (elements.size() > 1) {
            elements = elementsWithId(elements);
            if (elements.size() == 1) {
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
