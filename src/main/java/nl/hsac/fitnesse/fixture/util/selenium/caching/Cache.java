package nl.hsac.fitnesse.fixture.util.selenium.caching;

/**
 * Interface describing caches.
 */
public interface Cache<T> {
    /**
     * @return cached value if still valid, new value otherwise.
     */
    T getValue();

    /**
     * @return obtains new value (does not store it as cached value).
     */
    T getNewValue();

    /**
     * @return time (in ms since epoch) until which value will be considered valid.
     */
    long getValidUntil();

    /**
     * Changes stored value.
     * @param value new value to store.
     * @param validUntil new valid until timestamp.
     */
    void setValue(T value, long validUntil);

    /**
     * @return whether cache value can still be used.
     */
    default boolean hasValidValue() {
        return getValidUntil() < ElementCache.getTime();
    }

    /**
     * Ensures next value retrieved will be fresh (i.e. not cached).
     */
    default void clear() {
        setValue(null, 0);
    }
}
