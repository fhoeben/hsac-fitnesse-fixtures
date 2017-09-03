package nl.hsac.fitnesse.fixture.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Helper to obtain caches.
 */
public class CacheHelper {
    public static <K,V> Map<K,V> lruCache(final int maxSize) {
        return new LinkedHashMap<K, V>(maxSize * 4 / 3, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > maxSize;
            }
        };
    }

}
