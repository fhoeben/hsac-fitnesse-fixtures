package nl.hsac.fitnesse.fixture.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper to create derived values from dates.
 */
public class DatesHelper {
    private String dayPattern = "%s_day";
    private String monthPattern = "%s_month";
    private String yearPattern = "%s_year";
    private static final Pattern XML_DATE = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})");
    private static final Pattern NL_DATE = Pattern.compile("(\\d{1,2})-(\\d{1,2})-(\\d{4})");

    /**
     * Adds derived values for dates in map.
     * @param values values as provided.
     */
    public void addDerivedDates(Map<String, Object> values) {
        Map<String, Object> valuesToAdd = new HashMap<String, Object>();

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String key = entry.getKey();
            Object object = entry.getValue();
            if (object != null) {
                String stringValue = object.toString();
                Matcher matcher = XML_DATE.matcher(stringValue);
                if (matcher.matches()) {
                    handleXmlMatch(matcher, valuesToAdd, key);
                } else {
                    matcher = NL_DATE.matcher(stringValue);
                    if (matcher.matches()) {
                        handleNLMatch(matcher, valuesToAdd, key);
                    }
                }
            }
        }
        values.putAll(valuesToAdd);
    }

    private void addDerivedDates(Map<String, Object> valuesToAddTo, String baseKey, String day, String month, String year) {
        valuesToAddTo.put(String.format(dayPattern, baseKey), forceLength2(day));
        valuesToAddTo.put(String.format(monthPattern, baseKey), forceLength2(month));
        valuesToAddTo.put(String.format(yearPattern, baseKey), year);
    }

    private String forceLength2(String group) {
        return String.format("%02d", Integer.valueOf(group));
    }

    public String getDayPattern() {
        return dayPattern;
    }

    public void setDayPattern(String dayPattern) {
        this.dayPattern = dayPattern;
    }

    public String getMonthPattern() {
        return monthPattern;
    }

    public void setMonthPattern(String monthPattern) {
        this.monthPattern = monthPattern;
    }

    public String getYearPattern() {
        return yearPattern;
    }

    public void setYearPattern(String yearPattern) {
        this.yearPattern = yearPattern;
    }

    private void handleXmlMatch(Matcher matcher, Map<String, Object> valuesToAdd, String key) {
        String day = matcher.group(3);
        String month = matcher.group(2);
        String year = matcher.group(1);
        addDerivedDates(valuesToAdd, key, day, month, year);
    }

    private void handleNLMatch(Matcher matcher, Map<String, Object> valuesToAdd, String key) {
        String day = matcher.group(1);
        String month = matcher.group(2);
        String year = matcher.group(3);
        addDerivedDates(valuesToAdd, key, day, month, year);
    }

}
