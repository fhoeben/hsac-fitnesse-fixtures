package nl.hsac.fitnesse.fixture.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import javax.xml.datatype.DatatypeFactory;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.Assert.assertThrows;


/**
 * Calendar util test.
 */
public class CalendarUtilFailTest {
    /**
     * CalendarUtil.
     */
    private CalendarUtil calendarUtil;

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
        Calendar today = Calendar.getInstance();
        forceDatatypeFactoryException(() -> calendarUtil.buildXMLGregorianCalendarDate(today.getTime()));
    }

    /**
     * Test BuildXMLGregorianCalendar.
     */
    @Test
    public void testBuildXMLGregorianCalendarDateTimeException() {
        GregorianCalendar today = new GregorianCalendar();
        forceDatatypeFactoryException(() -> calendarUtil.buildXMLGregorianCalendarDateTime(today));
    }

    /**
     * Test Fail to BuildXMLGregorianCalendar.
     */
    @Test
    public void testFailBuildXMLGregorianCalendar() {
        forceDatatypeFactoryException(() -> calendarUtil.buildXMLGregorianCalendar());
    }

    /**
     * Test Fail to add to a Duration.
     */
    @Test
    public void testFailAddMonths() {
        forceDatatypeFactoryException(() -> calendarUtil.addMonths(2));
    }

    private void forceDatatypeFactoryException(ThrowingRunnable runnable) {
        System.setProperty(DATATYPE_FACTORY_PROPERTY, NON_EXISTENT_CLASS);

        assertThrows(EXPECTED_MESSAGE, IllegalStateException.class, runnable);
    }
}
