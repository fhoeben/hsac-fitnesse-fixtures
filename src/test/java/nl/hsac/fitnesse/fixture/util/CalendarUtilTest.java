package nl.hsac.fitnesse.fixture.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Calendar util test.
 */
public class CalendarUtilTest {

    public static final String DATATYPE_FACTORY_PROPERTY = "javax.xml.datatype.DatatypeFactory";

    /**
     * CalendarUtil.
     */
    private CalendarUtil calendarUtil;

    /**
     * Setup.
     */
    @Before
    public void setup() {
        calendarUtil = new CalendarUtil();
    }

    /**
     * Reset DataTypeFactory after every test.
     */
    @After
    public void tearDown() {
        System.getProperties().remove(DATATYPE_FACTORY_PROPERTY);
    }

    /**
     * Test BuildXMLGregorianCalendar.
     */
    @Test
    public void testBuildXMLGregorianCalendar() {
        Calendar today = Calendar.getInstance();
        XMLGregorianCalendar cal = calendarUtil.buildXMLGregorianCalendar();
        assertTrue(cal.getYear() == today.get(Calendar.YEAR));
        assertTrue(cal.getMonth() == today.get(Calendar.MONTH) + 1);
        assertTrue(cal.getDay() == today.get(Calendar.DAY_OF_MONTH));

        String dateString = cal.toXMLFormat();
        XMLGregorianCalendar calFromString = calendarUtil.buildXMLGregorianCalendar(dateString);
        assertEquals(cal, calFromString);
    }

    /**
     * Test BuildXMLGregorianCalendar.
     */
    @Test
    public void testBuildXMLGregorianCalendarWithDate() {
        Calendar today = Calendar.getInstance();
        XMLGregorianCalendar cal = calendarUtil.buildXMLGregorianCalendarDate(today.getTime());
        assertTrue(cal.getYear() == today.get(Calendar.YEAR));
        assertTrue(cal.getMonth() == today.get(Calendar.MONTH) + 1);
        assertTrue(cal.getDay() == today.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Tests handling of null inputs.
     */
    @Test
    public void testBuildXMLGregorianCalendarDateForNull() {
        assertNull(calendarUtil.buildXMLGregorianCalendarDate((XMLGregorianCalendar) null));
        assertNull(calendarUtil.buildXMLGregorianCalendarDate((Calendar) null));
        assertNull(calendarUtil.buildXMLGregorianCalendarDate((Date) null));
    }

    /**
     * Test BuildXMLGregorianCalendar.
     */
    @Test
    public void testBuildXMLGregorianCalendarDateTime() {
        GregorianCalendar today = new GregorianCalendar();
        XMLGregorianCalendar cal = calendarUtil.buildXMLGregorianCalendarDateTime(today);
        assertTrue(cal.getYear() == today.get(Calendar.YEAR));
        assertTrue(cal.getMonth() == today.get(Calendar.MONTH) + 1);
        assertTrue(cal.getDay() == today.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Test AddMonths.
     * Result should be months +2 and no TimeZoneShift
     */
    @Test
    public void testAddMonths() {
        XMLGregorianCalendar cal = calendarUtil.buildXMLGregorianCalendar();
        int tz = cal.getTimezone();
        XMLGregorianCalendar result = addMonths(2, cal);
        assertEquals(8, result.getMonth());
        assertEquals(0, result.getTimezone() - tz);
    }

    /**
     * Test AddMonths.
     * Result should be months +2 and no TimeZoneShift
     */
    @Test
    public void testSubstractMonths() {
        XMLGregorianCalendar cal = calendarUtil.buildXMLGregorianCalendar();
        int tz = cal.getTimezone();
        XMLGregorianCalendar result = addMonths(-2, cal);
        assertEquals(4, result.getMonth());
        assertEquals(0, result.getTimezone() - tz);
    }

    private XMLGregorianCalendar addMonths(int amount, XMLGregorianCalendar cal) {
        cal.setMonth(6);
        cal.setDay(1);
        cal.setHour(0);
        cal.setMinute(0);
        cal.setSecond(0);
        cal.setMillisecond(0);
        return calendarUtil.addMonths(cal, amount);
    }

    private XMLGregorianCalendar addYears(int amount, XMLGregorianCalendar cal) {
        cal.setYear(2000);
        cal.setMonth(6);
        cal.setDay(1);
        cal.setHour(0);
        cal.setMinute(0);
        cal.setSecond(0);
        cal.setMillisecond(0);
        return calendarUtil.addYears(cal, amount);
    }

    /**
     * Test addYears.
     */
    @Test
    public void testAddYears() {
        XMLGregorianCalendar cal = calendarUtil.buildXMLGregorianCalendar();
        int tz = cal.getTimezone();
        XMLGregorianCalendar result = addYears(2, cal);
        assertEquals(2002, result.getYear());
        assertEquals(0, result.getTimezone() - tz);
    }

    /**
     * Test addYears.
     */
    @Test
    public void testSubstractYears() {
        XMLGregorianCalendar cal = calendarUtil.buildXMLGregorianCalendar();
        int tz = cal.getTimezone();
        XMLGregorianCalendar result = addYears(-2, cal);
        assertEquals(1998, result.getYear());
        assertEquals(0, result.getTimezone() - tz);
    }

    /**
     * Test AddMonths into winterTime.
     * Result should be moths +2 and a one hour shift to the left.
     *
     */
    @Test
    public void testAddMonthsIntoWinterTime() {
        XMLGregorianCalendar cal = calendarUtil.buildXMLGregorianCalendar();
        cal.setMonth(10);
        cal.setDay(1);
        cal.setHour(0);
        cal.setMinute(0);
        cal.setSecond(0);
        cal.setMillisecond(0);
        XMLGregorianCalendar result = calendarUtil.addMonths(cal, 2);
        assertEquals(12, result.getMonth());
    }

    /**
     * Test AddMonths into winterTime.
     * Result should be moths +2 and a one hour shift to the right.
     *
     */
    @Test
    public void testAddMonthsIntoSummerTime() {
        XMLGregorianCalendar cal = calendarUtil.buildXMLGregorianCalendar();
        cal.setMonth(2);
        cal.setDay(1);
        cal.setHour(0);
        cal.setMinute(0);
        cal.setSecond(0);
        cal.setMillisecond(0);
        XMLGregorianCalendar result = calendarUtil.addMonths(cal, 2);
        assertEquals(4, result.getMonth());
    }
}
