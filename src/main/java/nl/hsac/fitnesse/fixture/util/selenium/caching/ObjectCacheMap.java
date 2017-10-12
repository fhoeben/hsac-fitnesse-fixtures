package nl.hsac.fitnesse.fixture.util.selenium.caching;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Maintains a map of ObjectCache instances.
 */
public class ObjectCacheMap<K, V> {
    private final Map<K, ObjectCache<V>> valuesCache = new HashMap<>();
    private final Function<K, ObjectCache<V>> cacheCreationFunction;

    public ObjectCacheMap(Function<K, V> function) {
        cacheCreationFunction = x -> new ObjectCache<>(() -> function.apply(x));
    }

    public V getValue(K key) {
        return getObjectCache(key).getValue();
    }

    public void putAll(Map<K, V> newValues, long timestamp) {
        newValues.forEach((k,v) -> getObjectCache(k).setValue(v, timestamp));
    }

    public ObjectCache<V> getObjectCache(K key) {
        return valuesCache.computeIfAbsent(key, cacheCreationFunction);
    }

    public Map<K, ObjectCache<V>> getValuesCache() {
        return valuesCache;
    }
}
