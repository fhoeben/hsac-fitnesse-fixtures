package nl.hsac.fitnesse.fixture.util.selenium.caching;

import org.openqa.selenium.remote.RemoteWebElement;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Our version of RemoteWebElement, optimizing calls to the server to obtain values.
 */
public class CachingRemoteWebElement extends RemoteWebElement {
    private final BooleanCache isDisplayedCache = new BooleanCache(super::isDisplayed);
    private final BooleanCache isEnabledCache = new BooleanCache(super::isEnabled);
    private final ObjectCache<String> tagNameCache = new ObjectCache<>(super::getTagName);
    private final ObjectCache<String> textCache = new ObjectCache<>(super::getText);
    private final Map<String, ObjectCache<String>> attributesCache = new HashMap<>();
    private final Function<String, ObjectCache<String>> attributeCacheCreationFunction = x -> new ObjectCache(() -> super.getAttribute(x));

    public CachingRemoteWebElement(RemoteWebElement element) {
        if (element != null) {
            setId(element.getId());
        }
    }

    @Override
    public boolean isDisplayed() {
        return isDisplayedCache.getBooleanValue();
    }

    @Override
    public boolean isEnabled() {
        return isEnabledCache.getBooleanValue();
    }

    @Override
    public String getTagName() {
        return tagNameCache.getValue();
    }

    @Override
    public String getText() {
        return textCache.getValue();
    }

    @Override
    public String getAttribute(String name) {
        ObjectCache<String> cache = attributesCache.computeIfAbsent(name, attributeCacheCreationFunction);
        return cache.getValue();
    }
}
