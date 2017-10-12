package nl.hsac.fitnesse.fixture.util.selenium.caching;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.function.BooleanSupplier;

/**
 * A wrapper around a boolean that is valid for an amount of time before its supplier must be called again.
 */
public class BooleanCache implements Cache<Boolean> {
    private final static Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final BooleanSupplier supplier;
    private boolean cachedValue;
    private long validUntil;

    public BooleanCache(BooleanSupplier supplier) {
        this.supplier = supplier;
    }

    public boolean getBooleanValue() {
        long start = ElementCache.getTime();
        if (validUntil < start) {
            LOGGER.trace("Cache miss");
            boolean newValue = getNewBooleanValue();
            setBooleanValue(newValue, ElementCache.getValidityEnd(start));
        } else {
            LOGGER.debug("Cache hit");
        }
        return cachedValue;
    }

    @Override
    public Boolean getNewValue() {
        return getNewBooleanValue();
    }

    @Override
    public Boolean getValue() {
        return cachedValue;
    }

    @Override
    public long getValidUntil() {
        return validUntil;
    }

    @Override
    public void setValue(Boolean value, long validUntil) {
        if (value == null) {
            this.validUntil = validUntil;
        } else {
            setBooleanValue(value, validUntil);
        }
    }

    public void setBooleanValue(boolean value, long validUntil) {
        this.cachedValue = value;
        this.validUntil = validUntil;
    }

    protected boolean getNewBooleanValue() {
        return supplier.getAsBoolean();
    }
}
