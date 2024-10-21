package nl.hsac.fitnesse.fixture.util;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper to allow some map's values to be hidden in test reports.
 */
public class SecretMasker {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecretMasker.class);
    public Map<String, Object> replaceSecrets(Collection<String> keysToMask, Map<String, ?> originalMap) {
        boolean replaced = false;
        Map<String, Object> newMap = null;
        try {
            newMap = new LinkedHashMap<>(originalMap);
            replaced = replaceDirectSecrets(keysToMask, newMap);
            if (replaceNestedSecrets(keysToMask, newMap)) {
                replaced = true;
            }
        } catch (RuntimeException e) {
            LOGGER.error("Unable to remove secrets from: " + originalMap);
        }
        return replaced ? newMap : (Map<String, Object>) originalMap;
    }

    protected boolean replaceDirectSecrets(Iterable<String> keysToMask, Map<String, Object> newMap) {
        AtomicBoolean replaced = new AtomicBoolean(false);
        keysToMask.forEach(s -> {
            if (newMap.containsKey(s)) {
                newMap.replace(s, "*****");
                replaced.set(true);
            }
        });
        return replaced.get();
    }

    protected boolean replaceNestedSecrets(Collection<String> keysToMask, Map<String, Object> newMap) {
        AtomicBoolean replaced = new AtomicBoolean(false);
        newMap.entrySet().forEach(e -> {
            Object currentValue = e.getValue();
            if (currentValue instanceof Map) {
                Map<String, Object> safeValue = replaceSecrets(keysToMask, (Map<String, ?>) currentValue);
                if (currentValue != safeValue) {
                    newMap.replace(e.getKey(), safeValue);
                    replaced.set(true);
                }
            }});
        return replaced.get();
    }
}
