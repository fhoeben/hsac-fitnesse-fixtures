package nl.hsac.fitnesse.fixture.util;

import nl.hsac.fitnesse.fixture.slim.SlimFixtureException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapHelper {
    private static final Pattern LIST_INDEX_PATTERN = Pattern.compile("(\\S+)\\[(\\d+)\\]");
    private HtmlCleaner htmlCleaner = new HtmlCleaner();

    /**
     * Gets value from map.
     * @param map map to get value from.
     * @param name name of (possibly nested) property to get value from.
     * @return value found, if it could be found, null otherwise.
     */
    public Object getValue(Map<String, Object> map, String name) {
        String cleanName = htmlCleaner.cleanupValue(name);
        return getValueImpl(map, cleanName);
    }

    protected Object getValueImpl(Map<String, Object> map, String name) {
        Object value = null;
        if (map.containsKey(name)) {
            value = map.get(name);
        } else {
            String[] parts = name.split("\\.", 2);
            if (parts.length > 1) {
                Object nested = getValueImpl(map, parts[0]);
                if (nested instanceof Map) {
                    Map<String, Object> nestedMap = (Map<String, Object>) nested;
                    value = getValueImpl(nestedMap, parts[1]);
                }
            } else if (isListName(name)) {
                value = getListValue(map, name);
            } else if (isListIndexExpr(name)) {
                value = getIndexedListValue(map, name);
            }
        }
        return value;
    }

    /**
     * Stores value in map.
     * @param value value to be passed.
     * @param name name to use this value for.
     * @param map map to store value in.
     */
    public void setValueForIn(Object value, String name, Map<String, Object> map) {
        if (isListName(name)) {
            String valueStr = null;
            if (value != null) {
                valueStr = value.toString();
            }
            setValuesForIn(valueStr, stripListIndicator(name), map);
        } else {
            if (name.endsWith("\\[]")) {
                name = name.replace("\\[]", "[]");
            }
            String cleanName = htmlCleaner.cleanupValue(name);
            Object cleanValue = value;
            if (value instanceof String) {
                cleanValue = htmlCleaner.cleanupValue((String) value);
            }
            if (map.containsKey(cleanName)) {
                // overwrite current value
                map.put(cleanName, cleanValue);
            } else {
                int firstDot = cleanName.indexOf(".");
                if (firstDot > -1) {
                    String key = cleanName.substring(0, firstDot);
                    Object nested = getValue(map, key);
                    if (nested == null) {
                        nested = new LinkedHashMap<String, Object>();
                        map.put(key, nested);
                    }
                    if (nested instanceof Map) {
                        Map<String, Object> nestedMap = (Map<String, Object>) nested;
                        String lastPart = cleanName.substring(firstDot + 1);
                        setValueForIn(cleanValue, lastPart, nestedMap);
                    } else {
                        throw new SlimFixtureException(false, key + " is not a map, but " + nested.getClass());
                    }
                } else if (isListIndexExpr(name)) {
                    setIndexedListValue(map, cleanName, cleanValue);
                } else {
                    map.put(cleanName, cleanValue);
                }
            }
        }
    }

    /**
     * Stores list of values in map.
     * @param values comma separated list of values.
     * @param name name to use this list for.
     * @param map map to store values in.
     */
    public void setValuesForIn(String values, String name, Map<String, Object> map) {
        String cleanName = htmlCleaner.cleanupValue(name);
        String[] valueArrays = values.split("\\s*,\\s*");
        List<Object> valueObjects = new ArrayList<Object>(valueArrays.length);
        for (int i = 0; i < valueArrays.length; i++) {
            String cleanValue = htmlCleaner.cleanupValue(valueArrays[i]);
            valueObjects.add(cleanValue);
        }
        setValueForIn(valueObjects, cleanName, map);
    }

    /**
     * Determines whether map one's content matches two.
     * @param one map the check content of.
     * @param two other map to check.
     * @return true if both maps are equal.
     */
    public boolean contentOfEquals(Map<String, Object> one, Object two) {
        if (one == null) {
            return two == null;
        } else {
            return one.equals(two);
        }
    }

    /**
     * Determines size of either (Map or Collection) value in the map.
     * @param expr expression indicating which (possibly nested) value in the map to determine size of.
     * @param map map to find value in.
     * @return size of value.
     * @throws SlimFixtureException if the value found is not a Map or Collection.
     */
    public int sizeOfIn(String expr, Map<String, Object> map) {
        int result;
        Object val = getValue(map, expr);
        if (val instanceof Map) {
            result = ((Map) val).size();
        } else if (val instanceof Collection) {
            result = ((Collection) val).size();
        } else {
            throw new SlimFixtureException(false, expr + " is not a collection");
        }
        return result;
    }

    protected Object getListValue(Map<String, Object> map, String name) {
        return getValue(map, stripListIndicator(name));
    }

    protected Object getIndexedListValue(Map<String, Object> map, String name) {
        Object value;
        String prop = getListKeyName(name);
        Object val = getValue(map, prop);
        if (val instanceof List) {
            List list = (List) val;
            int index = getListIndex(name);
            if (index < list.size()) {
                value = list.get(index);
            } else {
                value = null;
            }
        } else {
            throw new SlimFixtureException(false, prop + " is not a list, but " + val);
        }
        return value;
    }

    protected void setIndexedListValue(Map<String, Object> map, String name, Object value) {
        String prop = getListKeyName(name);
        Object val = getValue(map, prop);
        if (val == null) {
            val = new ArrayList<Object>();
            setValueForIn(val, prop, map);
        }
        if (val instanceof List) {
            List list = (List) val;
            int index = getListIndex(name);
            while (list.size() <= index) {
                list.add(null);
            }
            list.set(index, value);
        } else {
            throw new SlimFixtureException(false, prop + " is not a list, but " + val);
        }
    }

    protected boolean isListName(String name) {
        return name.endsWith("[]") && !name.endsWith("\\[]");
    }

    protected String stripListIndicator(String key) {
        return key.substring(0, key.length() - 2);
    }

    protected boolean isListIndexExpr(String key) {
        return getListIndexMatcher(key).matches();
    }

    protected String getListKeyName(String key) {
        Matcher matcher = getListIndexMatcher(key);
        matcher.matches();
        return matcher.group(1);
    }

    protected int getListIndex(String key) {
        Matcher matcher = getListIndexMatcher(key);
        matcher.matches();
        return Integer.parseInt(matcher.group(2));
    }

    protected Matcher getListIndexMatcher(String key) {
        return LIST_INDEX_PATTERN.matcher(key);
    }

    public void setHtmlCleaner(HtmlCleaner htmlCleaner) {
        this.htmlCleaner = htmlCleaner;
    }
}
