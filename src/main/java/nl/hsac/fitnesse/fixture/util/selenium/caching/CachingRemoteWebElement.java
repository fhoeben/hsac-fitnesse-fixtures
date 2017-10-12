package nl.hsac.fitnesse.fixture.util.selenium.caching;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.remote.RemoteWebElement;

/**
 * Our version of RemoteWebElement, optimizing calls to the server to obtain values.
 */
public class CachingRemoteWebElement extends RemoteWebElement {
    private BooleanCache isSelectedCache;
    private final BooleanCache isDisplayedCache = new BooleanCache(super::isDisplayed);
    private BooleanCache isEnabledCache;
    private ObjectCache<String> tagNameCache;
    private ObjectCache<String> textCache;
    private ObjectCache<Point> locationCache;
    private ObjectCache<Dimension> sizeCache;
    private ObjectCache<Rectangle> rectCache;

    private ObjectCacheMap<String, String> attributesCache;
    private ObjectCacheMap<String, String> cssValuesCache;

    public CachingRemoteWebElement(RemoteWebElement element) {
        if (element != null) {
            setId(element.getId());
        }
    }

    @Override
    public boolean isSelected() {
        if (isSelectedCache == null) {
            isSelectedCache = new BooleanCache(super::isSelected);
        }
        return isSelectedCache.getBooleanValue();
    }

    @Override
    public boolean isDisplayed() {
        return isDisplayedCache.getBooleanValue();
    }

    @Override
    public boolean isEnabled() {
        if (isEnabledCache == null) {
            isEnabledCache = new BooleanCache(super::isEnabled);
        }
        return isEnabledCache.getBooleanValue();
    }

    @Override
    public String getTagName() {
        if (tagNameCache == null) {
            tagNameCache = new ObjectCache<>(super::getTagName);
        }
        return tagNameCache.getValue();
    }

    @Override
    public String getText() {
        if (textCache == null) {
            textCache = new ObjectCache<>(super::getText);
        }
        return textCache.getValue();
    }

    @Override
    public Point getLocation() {
        if (locationCache == null) {
            locationCache = new ObjectCache<>(super::getLocation);
        }
        return locationCache.getValue();
    }

    @Override
    public Dimension getSize() {
        if (sizeCache == null) {
            sizeCache = new ObjectCache<>(super::getSize);
        }
        return sizeCache.getValue();
    }

    @Override
    public Rectangle getRect() {
        if (rectCache == null) {
            rectCache = new ObjectCache<>(super::getRect);
        }
        return rectCache.getValue();
    }

    @Override
    public String getAttribute(String name) {
        if (attributesCache == null) {
            attributesCache = new ObjectCacheMap<>(super::getAttribute);
        }
        return attributesCache.getValue(name);
    }

    @Override
    public String getCssValue(String propertyName) {
        if (cssValuesCache == null) {
            cssValuesCache = new ObjectCacheMap<>(super::getCssValue);
        }
        return cssValuesCache.getValue(propertyName);
    }
}
