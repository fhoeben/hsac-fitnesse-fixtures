package nl.hsac.fitnesse.fixture.slim;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Fixture to add or subtract timeunits from a given date or time
 */

public class DateTimeCalculatorFixture extends SlimFixture {
    private String dateFormat;
    private DateFormatFixture dff;

    public DateTimeCalculatorFixture(String dateFormat) {
        setDateFormat(dateFormat);
    }

    public void setDateFormat(String df) {
        this.dateFormat = df;
        dff = new DateFormatFixture(dateFormat);
    }

    public String addYearsTo(int years, String formattedDate) {
        return addUnitsToDate(Calendar.YEAR, years, formattedDate);
    }

    public String addMonthsTo(int months, String formattedDate) {
        return addUnitsToDate(Calendar.MONTH, months, formattedDate);
    }

    public String addDaysTo(int days, String formattedDate) {
        return addUnitsToDate(Calendar.DATE, days, formattedDate);
    }

    public String addHoursTo(int hours, String formattedDate) {
        return addUnitsToDate(Calendar.HOUR, hours, formattedDate);
    }

    public String addMinutesTo(int minutes, String formattedDate) {
        return addUnitsToDate(Calendar.MINUTE, minutes, formattedDate);
    }

    public String addSecondsTo(int seconds, String formattedDate) {
        return addUnitsToDate(Calendar.SECOND, seconds, formattedDate);
    }

    private String addUnitsToDate(int unit, int number, String formattedDate) {
        Date d = dff.parseDate(formattedDate);
        DateFormat df = dff.getDateFormat(dateFormat);
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.add(unit, number);
        d = c.getTime();
        return df.format(d);
    }
}
