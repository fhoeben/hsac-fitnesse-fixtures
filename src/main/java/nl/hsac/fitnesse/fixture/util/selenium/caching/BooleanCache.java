package nl.hsac.fitnesse.fixture.util.selenium.caching;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.function.BooleanSupplier;

/**
 * A wrapper around a boolean that is valid for an amount of time before its supplier must be called again.
 */
public class BooleanCache {
    private final static Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final BooleanSupplier supplier;
    private boolean cachedValue;
    private long validUntil;

    public BooleanCache(BooleanSupplier supplier) {
        this.supplier = supplier;
    }

    public boolean getValue() {
        long start = ElementCache.getTime();
        if (validUntil < start) {
            LOGGER.trace("Cache miss");
            cachedValue = getCachedBoolean();
            validUntil = ElementCache.getValidityEnd(start);
        } else {
            LOGGER.debug("Cache hit");
        }
        return cachedValue;
    }

    protected boolean getCachedBoolean() {
        return supplier.getAsBoolean();
    }
}
