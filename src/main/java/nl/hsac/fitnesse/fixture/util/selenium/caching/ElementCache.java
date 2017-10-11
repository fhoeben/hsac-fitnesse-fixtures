package nl.hsac.fitnesse.fixture.util.selenium.caching;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

/**
 * Central point for generic remote element caching settings.
 */
public class ElementCache {
    private final static Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static long maxCacheDuration = 500;
    private static long minCacheDuration = 100;
    private static int ageFactor = 3;

    public static long getTime() {
        return System.currentTimeMillis();
    }

    public static long getValidityEnd(long start) {
        long end = getTime();
        long cacheDuration;
        if (ageFactor > 0) {
            long duration = end - start;
            LOGGER.trace("Cache value obtained in: {} ms", duration);
            long autoAge = duration * ageFactor;
            cacheDuration = Math.max(autoAge, minCacheDuration);
        } else {
            cacheDuration = maxCacheDuration;
        }
        LOGGER.debug("New element will be valid for: {} ms", cacheDuration);
        return end + cacheDuration;
    }

    public static void setMaxCacheDuration(long maxCacheDuration) {
        ElementCache.maxCacheDuration = maxCacheDuration;
    }

    public static long getMaxCacheDuration() {
        return maxCacheDuration;
    }

    public static int getAgeFactor() {
        return ageFactor;
    }

    public static void setAgeFactor(int ageFactor) {
        ElementCache.ageFactor = ageFactor;
    }

    public static long getMinCacheDuration() {
        return minCacheDuration;
    }

    public static void setMinCacheDuration(long minCacheDuration) {
        ElementCache.minCacheDuration = minCacheDuration;
    }
}
