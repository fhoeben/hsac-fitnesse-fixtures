package nl.hsac.fitnesse.fixture.util;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Utility class for Calendar and Date methods.
 */
public final class CalendarUtil {

    /**
     * Create a XMLGregorianCalendar.
     * 
     * @return a XMLGregorianCalendar
     */
    public XMLGregorianCalendar buildXMLGregorianCalendar() {
        return buildXMLGregorianCalendarDate(new GregorianCalendar());
    }

    /**
     * Add Days to a Gregorian Calendar.
     * 
     * @param cal The XMLGregorianCalendar source
     * @param amount The amount of days. Can be a negative Integer to substract.
     * @return A XMLGregorianCalendar with the new Date
     */
    public XMLGregorianCalendar addDays(final XMLGregorianCalendar cal, final int amount) {
        XMLGregorianCalendar to = buildXMLGregorianCalendarDate(cal);
        // Add amount of months
        to.add(addDays(amount));
        return to;
    }

    /**
     * Add Months to a Gregorian Calendar.
     * 
     * @param cal The XMLGregorianCalendar source
     * @param amount The amount of months. Can be a negative Integer to
     *            substract.
     * @return A XMLGregorianCalendar with the new Date
     */
    public XMLGregorianCalendar addMonths(final XMLGregorianCalendar cal, final int amount) {
        XMLGregorianCalendar to = buildXMLGregorianCalendarDate(cal);
        // Add amount of months
        to.add(addMonths(amount));
        return to;
    }

    /**
     * Add Years to a Gregorian Calendar.
     * 
     * @param cal The XMLGregorianCalendar source
     * @param amount The amount of years. Can be a negative Integer to
     *            substract.
     * @return A XMLGregorianCalendar with the new Date
     */
    public XMLGregorianCalendar addYears(final XMLGregorianCalendar cal, final int amount) {
        XMLGregorianCalendar to = buildXMLGregorianCalendarDate(cal);
        // Add amount of months
        to.add(addYears(amount));
        return to;
    }

    /**
     * Create a XMLGregorianCalendar Without Time Component.
     * 
     * @param cal a XMLGregorianCalendar, possibly with time information.
     * @return An XMLGregorianCalendar
     */
    public XMLGregorianCalendar buildXMLGregorianCalendarDate(XMLGregorianCalendar cal) {
        XMLGregorianCalendar result = null;
        if (cal != null) {
            result = newXMLGregorianCalendar(cal.getDay(), cal.getMonth(), cal.getYear());
        }
        return result;
    }

    /**
     * Create a XMLGregorianCalendar Without Time Component.
     * 
     * @param date The initial Date
     * @return An XMLGregorianCalendar
     */
    public XMLGregorianCalendar buildXMLGregorianCalendarDate(Date date) {
        XMLGregorianCalendar result = null;
        if (date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            result = buildXMLGregorianCalendarDate(cal);
        }
        return result;
    }

    /**
     * Create a XMLGregorianCalendar Without Time Component.
     * 
     * @param cal The initial Calendar
     * @return An XMLGregorianCalendar
     */
    public XMLGregorianCalendar buildXMLGregorianCalendarDate(Calendar cal) {
        XMLGregorianCalendar result = null;
        if (cal != null) {
            result = newXMLGregorianCalendar(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.YEAR));
        }
        return result;
    }

    private XMLGregorianCalendar newXMLGregorianCalendar(int day, int month, int year) {
        return getDatatypeFactory().newXMLGregorianCalendar(year, month, day, DatatypeConstants.FIELD_UNDEFINED,
                DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED,
                DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED);
    }

    /**
     * Create a XMLGregorianCalendar based on XML string value.
     * 
     * @param dateString the XML Date to parse
     * @return An XMLGregorianCalendar
     */
    public XMLGregorianCalendar buildXMLGregorianCalendar(String dateString) {
        return getDatatypeFactory().newXMLGregorianCalendar(dateString);
    }

    /**
     * Create a XMLGregorianCalendar With Time Component.
     * 
     * @param cal The initial GragorianCalendar
     * @return An XMLGregorianCalendar
     */
    public XMLGregorianCalendar buildXMLGregorianCalendarDateTime(GregorianCalendar cal) {
        return getDatatypeFactory().newXMLGregorianCalendar(cal);
    }

    /**
     * Determines number of years between two dates so that premium duration can
     * be established.
     * 
     * @param startDate start date of a period (e.g. date of birth of insured
     *            party).
     * @param endDate end date (e.g. premium end date of an insurance).
     * @return number of years between start and end.
     */
    public int getDurationInYears(XMLGregorianCalendar startDate, XMLGregorianCalendar endDate) {
        int startYear = startDate.getYear();
        final int dec = 12;
        if (startDate.getMonth() == dec) {
            // started in December, increase year with one
            startYear++;
        }
        int endYear = endDate.getYear();
        return endYear - startYear;
    }

    /**
     * Get an XMLGregorianCalendar that holds the first day of the current
     * month.
     * 
     * @return current first of month
     */
    public XMLGregorianCalendar getCurrentFirstOfMonthDate() {
        XMLGregorianCalendar result = buildXMLGregorianCalendar();
        result.setDay(1);
        return result;
    }

    /**
     * Create a Duration of x days.
     * 
     * @param amount The number of days. Use negative numbers to substract days
     * @return a Duration
     */
    Duration addDays(final int amount) {
        Duration duration;
        if (amount < 0) {
            duration = getDatatypeFactory().newDuration(false, 0, 0, Math.abs(amount), 0, 0, 0);
        } else {
            duration = getDatatypeFactory().newDuration(true, 0, 0, amount, 0, 0, 0);
        }
        return duration;
    }

    /**
     * Create a Duration of x months.
     * 
     * @param amount The number of months. Use negative numbers to substract
     *            Months
     * @return a Duration
     */
    Duration addMonths(final int amount) {
        Duration duration;
        if (amount < 0) {
            duration = getDatatypeFactory().newDuration(false, 0, Math.abs(amount), 0, 0, 0, 0);
        } else {
            duration = getDatatypeFactory().newDuration(true, 0, amount, 0, 0, 0, 0);
        }
        return duration;
    }

    /**
     * Create a Duration of x years.
     * 
     * @param amount The number of years. Use negative numbers to substract
     *            Yeras
     * @return a Duration
     */
    Duration addYears(final int amount) {
        Duration duration;
        if (amount < 0) {
            duration = getDatatypeFactory().newDuration(false, Math.abs(amount), 0, 0, 0, 0, 0);
        } else {
            duration = getDatatypeFactory().newDuration(true, amount, 0, 0, 0, 0, 0);
        }
        return duration;
    }

    private DatatypeFactory getDatatypeFactory() {
        try {
            return DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new IllegalStateException("Incorrect configuration, unable to get DatatypeFactory instance", e);
        }
    }
}
