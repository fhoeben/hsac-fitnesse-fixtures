package nl.hsac.fitnesse.fixture.slim;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Fixture that helps converting dates to and from various formats. Intended to be used as a utility fixture i.e. as library
 */

public class DateFormatFixture extends SlimFixture {
    private String dateFormat = "dd-MM-yyyy";
    private TimeZone timezone = TimeZone.getTimeZone("GMT+1");
    private boolean timestampHasMilliseconds = true;

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
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        sdf.setTimeZone(timezone);
        return sdf.format(date);
    }

    public String formatDateAs(String date, String newFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        sdf.setTimeZone(timezone);
        SimpleDateFormat targetFormat = new SimpleDateFormat(newFormat);
        targetFormat.setTimeZone(timezone);
        String formattedDate;
        try {
            formattedDate = targetFormat.format(sdf.parse(date));
        } catch (ParseException e) {
            throw new SlimFixtureException(false, e.getMessage(), e);
        }
        return formattedDate;
    }

    public long timestampForDate(String date) {
        long timeStamp;
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        sdf.setTimeZone(timezone);
        try {
            timeStamp = sdf.parse(date).getTime();
            if (!timestampHasMilliseconds) {
                timeStamp = timeStamp / 1000L;
            }
        } catch (ParseException e) {
            throw new SlimFixtureException(false, e.getMessage(), e);
        }
        return timeStamp;
    }
}

