package nl.hsac.fitnesse.fixture.util.selenium.caching;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.remote.RemoteWebElement;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Our version of RemoteWebElement, optimizing calls to the server to obtain values.
 */
public class CachingRemoteWebElement extends RemoteWebElement {
    private final BooleanCache isSelectedCache = new BooleanCache(super::isSelected);
    private final BooleanCache isDisplayedCache = new BooleanCache(super::isDisplayed);
    private final BooleanCache isEnabledCache = new BooleanCache(super::isEnabled);
    private final ObjectCache<String> tagNameCache = new ObjectCache<>(super::getTagName);
    private final ObjectCache<String> textCache = new ObjectCache<>(super::getText);
    private final ObjectCache<Point> locationCache = new ObjectCache<>(super::getLocation);
    private final ObjectCache<Dimension> sizeCache = new ObjectCache<>(super::getSize);
    private final ObjectCache<Rectangle> rectCache = new ObjectCache<>(super::getRect);

    private Map<String, ObjectCache<String>> attributesCache;
    private Function<String, ObjectCache<String>> attributeCacheCreationFunction;

    private Map<String, ObjectCache<String>> cssValuesCache;
    private Function<String, ObjectCache<String>> cssCacheCreationFunction;

    public CachingRemoteWebElement(RemoteWebElement element) {
        if (element != null) {
            setId(element.getId());
        }
    }

    @Override
    public boolean isSelected() {
        return isSelectedCache.getBooleanValue();
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
    public Point getLocation() {
        return locationCache.getValue();
    }

    @Override
    public Dimension getSize() {
        return sizeCache.getValue();
    }

    @Override
    public Rectangle getRect() {
        return rectCache.getValue();
    }

    @Override
    public String getAttribute(String name) {
        if (attributesCache == null) {
            attributeCacheCreationFunction = x -> new ObjectCache<>(() -> super.getAttribute(x));
            attributesCache = new HashMap<>();
        }
        ObjectCache<String> cache = attributesCache.computeIfAbsent(name, attributeCacheCreationFunction);
        return cache.getValue();
    }

    @Override
    public String getCssValue(String propertyName) {
        if (cssValuesCache == null) {
            cssCacheCreationFunction = x -> new ObjectCache<>(() -> super.getCssValue(x));
            cssValuesCache = new HashMap<>();
        }
        ObjectCache<String> cache = cssValuesCache.computeIfAbsent(propertyName, cssCacheCreationFunction);
        return cache.getValue();
    }
}
