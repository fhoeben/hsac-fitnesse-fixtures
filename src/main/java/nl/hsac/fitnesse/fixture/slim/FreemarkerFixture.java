package nl.hsac.fitnesse.fixture.slim;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Fixture to generate strings using Freemarker templates. These strings can be stored in variables so the can
 * be passed as arguments to methods of other fixtures.
 * This fixture can be used using Slim's dynamic decision tables or using scripts (and scenarios).
 */
public class FreemarkerFixture extends SlimFixture {
    private final String defaultTemplate;

    private final Map<String, Object> currentValues = new LinkedHashMap<String, Object>();

    /**
     * Creates new without default template.
     */
    public FreemarkerFixture() {
        this(null);
    }

    /**
     * Creates new with a default template, this is used in dynamic decision tables to prevent having to specify
     * the template in the header row (any output column just gets the result of applying this templates).
     * @param defaultTemplate location (below /templates/) of Freemarker template.
     */
    public FreemarkerFixture(String defaultTemplate) {
        this.defaultTemplate = defaultTemplate;
    }

    /**
     * Applies template to current values.
     * @param aTemplate name of template file (relative to 'testdata' directory on classpath)
     * @return template result.
     */
    public String applyTemplate(String aTemplate) {
        String result = getEnvironment().processTemplate(aTemplate, currentValues);
        try {
            if (aTemplate.contains(".xml")) {
                result = getEnvironment().getHtmlForXml(result);
            } else if (aTemplate.contains(".json")) {
                result = getEnvironment().getHtmlForJson(result);
            }
        } catch (Exception e) {
            // can not be formatted, return raw result
        }
        return result;
    }

    /**
     * Stores value to be passed to template, or GET.
     * @param value value to be passed.
     * @param name name to use this value for.
     */
    public void setValueFor(Object value, String name) {
        String cleanName = cleanupValue(name);
        Object cleanValue = value;
        if (value instanceof String) {
            cleanupValue((String) value);
        }
        currentValues.put(cleanName, cleanValue);
    }

    /**
     * Stores list of values to be passed to template.
     * @param values comma separated list of values.
     * @param name name to use this list for.
     */
    public void setValuesFor(String values, String name) {
        String cleanName = cleanupValue(name);
        String[] valueArrays = values.split("\\s*,\\s*");
        for (int i = 0; i < valueArrays.length; i++) {
            valueArrays[i] = cleanupValue(valueArrays[i]);
        }
        currentValues.put(cleanName, valueArrays);
    }

    /**
     * Clears a values previously set.
     * @param name value to remove.
     * @return true if value was present.
     */
    public boolean clearValue(String name) {
        String cleanName = cleanupValue(name);
        boolean result = currentValues.containsKey(cleanName);
        currentValues.remove(cleanName);
        return result;
    }

    /**
     * Clears all values previously set.
     */
    public void clearValues() {
        currentValues.clear();
    }

    // methods to support usage in dynamic decision tables
    public void reset() {
        clearValues();
    }

    public void set(String key, Object value) {
        setValueFor(value, key);
    }

    public String get(String requestedValue) {
        String result;
        if (defaultTemplate == null) {
            result = applyTemplate(requestedValue);
        } else {
            result = applyTemplate(defaultTemplate);
        }
        return result;
    }

}
