package nl.hsac.fitnesse.fixture.util.selenium.caching;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.function.Supplier;

/**
 * A wrapper around an object that is valid for an amount of time before its supplier must be called again.
 */
public class ObjectCache<T> {
    private final static Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Supplier<? extends T> supplier;
    private T cachedValue;
    private long validUntil;

    public ObjectCache(Supplier<? extends T> supplier) {
        this.supplier = supplier;
    }

    public T getValue() {
        long start = ElementCache.getTime();
        if (validUntil < start) {
            LOGGER.trace("Cache miss");
            cachedValue = getCachedValue();
            validUntil = ElementCache.getValidityEnd(start);
        } else {
            LOGGER.debug("Cache hit");
        }
        return cachedValue;
    }

    protected T getCachedValue() {
        return supplier.get();
    }
}
