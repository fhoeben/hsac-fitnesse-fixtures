package nl.hsac.fitnesse.fixture.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.Assert.assertTrue;


/**
 * Calendar util test.
 */
public class CalendarUtilFailTest {
    /**
     * CalendarUtil.
     */
    private CalendarUtil calendarUtil;

    @Rule
    public ExpectedException expected = ExpectedException.none();

    private static final String EXPECTED_MESSAGE = "Incorrect configuration, unable to get DatatypeFactory instance";

    public static final String DATATYPE_FACTORY_PROPERTY = DatatypeFactory.DATATYPEFACTORY_PROPERTY;

    private static final String NON_EXISTENT_CLASS = "non.existent.class";

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
    public void testBuildXMLGregorianCalendarWithDateException() {
        forceDatatypeFactoryException();
        Calendar today = Calendar.getInstance();
        XMLGregorianCalendar cal = calendarUtil.buildXMLGregorianCalendarDate(today.getTime());
        assertTrue(cal.getYear() == today.get(Calendar.YEAR));
        assertTrue(cal.getMonth() == today.get(Calendar.MONTH) + 1);
        assertTrue(cal.getDay() == today.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Test BuildXMLGregorianCalendar.
     */
    @Test
    public void testBuildXMLGregorianCalendarDateTimeException() {
        forceDatatypeFactoryException();
        GregorianCalendar today = new GregorianCalendar();
        calendarUtil.buildXMLGregorianCalendarDateTime(today);
    }

    /**
     * Test Fail to BuildXMLGregorianCalendar.
     */
    @Test
    public void testFailBuildXMLGregorianCalendar() {
        forceDatatypeFactoryException();
        calendarUtil.buildXMLGregorianCalendar();
    }

    /**
     * Test Fail to add to a Duration.
     */
    @Test
    public void testFailAddMonths() {
        forceDatatypeFactoryException();
        calendarUtil.addMonths(2);
    }

    private void forceDatatypeFactoryException() {
        System.setProperty(DATATYPE_FACTORY_PROPERTY, NON_EXISTENT_CLASS);

        expected.expect(IllegalStateException.class);
        expected.expectMessage(EXPECTED_MESSAGE);
    }
}
