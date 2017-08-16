package nl.hsac.fitnesse.fixture.slim;

import java.text.DateFormat;
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
        if(!timestampHasMilliseconds) {
            timestamp = timestamp * 1000L;
        }

        Date date = new Date(timestamp);
        DateFormat sdf = getDateFormat(dateFormat);
        return sdf.format(date);
    }

    public String formatDateAs(String date, String newFormat) {
        DateFormat sdf = getConfiguredDateFormat();
        DateFormat targetFormat = getDateFormat(newFormat);
        String formattedDate;
        try {
            formattedDate = targetFormat.format(sdf.parse(date));
        } catch (ParseException e) {
            throw new SlimFixtureException(false, "Could not parse " + date + " using format: " + dateFormat, e);
        }
        return formattedDate;
    }

    public long timestampForDate(String date) {
        long timeStamp;
        DateFormat sdf = getConfiguredDateFormat();
        try {
            timeStamp = sdf.parse(date).getTime();
            if (!timestampHasMilliseconds) {
                timeStamp = timeStamp / 1000L;
            }
        } catch (ParseException e) {
            throw new SlimFixtureException(false, "Could not parse " + date + " using format: " + dateFormat, e);
        }
        return timeStamp;
    }

    protected DateFormat getConfiguredDateFormat() {
        return getDateFormat(dateFormat);
    }

    protected DateFormat getDateFormat(String dateFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        sdf.setTimeZone(timezone);
        return sdf;
    }
}

