package nl.hsac.fitnesse.fixture.util;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests DatesHelper.
 */
public class DatesHelperTest {
    private final DatesHelper helper = new DatesHelper();

    /**
     * Tests adding without dates.
     */
    @Test
    public void testAddNoDates() {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("datum", "asdad");
        values.put("null", null);
        values.put("int", 23);

        Map<String, Object> expected = new HashMap<String, Object>();
        expected.putAll(values);
        
        helper.addDerivedDates(values);

        assertEquals("Not same as input: " + values, expected, values);
    }

    /**
     * Tests adding with XML dates.
     */
    @Test
    public void testAddXMLDate() {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("datum", "asdad");
        values.put("date1", "1900-01-23");
        values.put("null", null);
        values.put("int", 23);
        values.put("date2", "1975-05-02");

        helper.addDerivedDates(values);

        checkKeyValue(values, "date1_day", "23");
        checkKeyValue(values, "date1_month", "01");
        checkKeyValue(values, "date1_year", "1900");
        checkKeyValue(values, "date1", "1900-01-23");

        checkKeyValue(values, "date2_day", "02");
        checkKeyValue(values, "date2_month", "05");
        checkKeyValue(values, "date2_year", "1975");
        checkKeyValue(values, "date2", "1975-05-02");
    }

    /**
     * Tests adding with NL dates.
     */
    @Test
    public void testAddNLDate() {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("datum", "asdad");
        values.put("date1", "23-1-1900");
        values.put("null", null);
        values.put("int", 23);
        values.put("date2", "2-05-1975");

        helper.addDerivedDates(values);

        checkKeyValue(values, "date1_day", "23");
        checkKeyValue(values, "date1_month", "01");
        checkKeyValue(values, "date1_year", "1900");
        checkKeyValue(values, "date1", "23-1-1900");

        checkKeyValue(values, "date2_day", "02");
        checkKeyValue(values, "date2_month", "05");
        checkKeyValue(values, "date2_year", "1975");
        checkKeyValue(values, "date2", "2-05-1975");
    }

    /**
     * Tests adding with own postfixes.
     */
    @Test
    public void testOwnPostfixes() {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("datum", "asdad");
        values.put("date1", "23-1-1900");
        values.put("null", null);
        values.put("int", 23);
        values.put("date2", "1975-02-01");

        helper.setDayPattern("%s_dag");
        helper.setMonthPattern("%s_maand");
        helper.setYearPattern("%s+jaar");
        helper.addDerivedDates(values);

        checkKeyValue(values, "date1_dag", "23");
        checkKeyValue(values, "date1_maand", "01");
        checkKeyValue(values, "date1+jaar", "1900");
        checkKeyValue(values, "date1", "23-1-1900");

        checkKeyValue(values, "date2_dag", "01");
        checkKeyValue(values, "date2_maand", "02");
        checkKeyValue(values, "date2+jaar", "1975");
        checkKeyValue(values, "date2", "1975-02-01");
    }

    private void checkKeyValue(Map<String, Object> values, String key, String expectedValue) {
        assertTrue("Did not contain key: " + key + ". " + values, values.containsKey(key));
        assertEquals("Wrong value for key: " + key, expectedValue, values.get(key));
    }
}
