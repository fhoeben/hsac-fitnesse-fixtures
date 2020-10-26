package nl.hsac.fitnesse.fixture.util.selenium.caching;

import org.openqa.selenium.remote.JsonToWebElementConverter;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

/**
 * Selenium element converter that ensure our {@link CachingRemoteWebElement} are used.
 */
public class WebElementConverter extends JsonToWebElementConverter {
    private final RemoteWebDriver driver;

    public WebElementConverter(RemoteWebDriver d) {
        super(d);
        driver = d;
    }

    @Override
    public Object apply(Object result) {
        if (result instanceof RemoteWebElement
                && !(result instanceof CachingRemoteWebElement)) {
            RemoteWebElement originalElement = (RemoteWebElement) result;
            result = createCachingWebElement(originalElement);
        }
        return super.apply(result);
    }

    @Override
    protected CachingRemoteWebElement newRemoteWebElement() {
        return createCachingWebElement(null);
    }

    protected CachingRemoteWebElement createCachingWebElement(RemoteWebElement originalElement) {
        CachingRemoteWebElement element = new CachingRemoteWebElement(originalElement);
        // ensure we always set the correct parent and file detector
        element.setParent(driver);
        element.setFileDetector(driver.getFileDetector());
        return element;
    }
}
