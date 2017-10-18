package nl.hsac.fitnesse.fixture.util.selenium.caching;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.remote.internal.JsonToWebElementConverter;

/**
 * Selenium element converter that ensure our {@link CachingRemoteWebElement} are used.
 */
public class WebElementConverter extends JsonToWebElementConverter {
    public WebElementConverter(RemoteWebDriver d) {
        super(d);
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
        return new CachingRemoteWebElement(originalElement);
    }
}
