package nl.hsac.fitnesse.fixture.slim;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Fixture that helps converting dates to and from various formats. Intended to be used as a utility fixture i.e. as library
 */

public class DateFormatFixture extends SlimFixture {
    private String dateFormat = "dd-MM-yyyy";
    private boolean timestampHasMilliseconds = true;

    public void setDateFormat(String df) {
        dateFormat = df;
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
        return sdf.format(date);
    }

    public String formatDateAs(String date, String newFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        SimpleDateFormat targetFormat = new SimpleDateFormat(newFormat);
        String formattedDate = null;
        try {
            formattedDate = targetFormat.format(sdf.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return formattedDate;
    }

    public long timestampForDate(String date) {
        long timeStamp = 0;
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

        try {
            timeStamp = sdf.parse(date).getTime();
            if (!timestampHasMilliseconds) {
                timeStamp = timeStamp / 1000L;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timeStamp;
    }
}

