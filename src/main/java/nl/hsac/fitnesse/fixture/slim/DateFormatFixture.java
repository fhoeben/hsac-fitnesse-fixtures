package nl.hsac.fitnesse.fixture.slim;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Fixture that helps converting dates to and from various formats. Intended to be used as a utility fixture i.e. as library
 */

public class DateFormatFixture extends SlimFixture {
    private String dateFormat = ((SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT)).toPattern();
    private TimeZone timezone = TimeZone.getDefault();
    private boolean timestampHasMilliseconds = true;

    public DateFormatFixture() {}
    public DateFormatFixture(String dateformat, String timezone) {
        setDateFormat(dateformat);
        setTimezone(timezone);
    }

    public void setDateFormat(String df) {
        dateFormat = df;
    }

    public void setTimezone(String newTimezone) {
        timezone = TimeZone.getTimeZone(newTimezone);
    }

    public void timestampHasMilliseconds(boolean hasMillis) {
        timestampHasMilliseconds = hasMillis;
    }

    public String formatTimestamp(long timestamp) {
        return formatTimestampAs(timestamp, dateFormat);
    }

    public String formatTimestampAs(long timestamp, String dateFormat) {
        SimpleDateFormat sdf = getDateFormat(dateFormat);
        if(!timestampHasMilliseconds) {
            timestamp = timestamp * 1000L;
        }

        Date date = new Date(timestamp);
        return sdf.format(date);
    }

    public String formatDateAs(String date, String newFormat) {
        Date parsedDate = parseDate(date);
        SimpleDateFormat targetFormat = getDateFormat(newFormat);
        return targetFormat.format(parsedDate);
    }

    public long timestampForDate(String date) {
        Date parsedDate = parseDate(date);
        long timeStamp = parsedDate.getTime();
        if (!timestampHasMilliseconds) {
            timeStamp = timeStamp / 1000L;
        }
        return timeStamp;
    }

    protected Date parseDate(String date) {
        SimpleDateFormat sdf = getConfiguredDateFormat();
        return parseDate(sdf, date);
    }

    protected Date parseDate(SimpleDateFormat sdf, String date) {
        try {
            return sdf.parse(date);
        } catch (ParseException e) {
            String pattern = sdf.toPattern();
            throw new SlimFixtureException(false, "Could not parse " + date + " using format: " + pattern, e);
        }
    }

    protected SimpleDateFormat getConfiguredDateFormat() {
        return getDateFormat(dateFormat);
    }

    protected SimpleDateFormat getDateFormat(String dateFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        sdf.setTimeZone(timezone);
        return sdf;
    }
}

