package nl.hsac.fitnesse.fixture.slim;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Fixture that helps converting dates to and from various formats. Intended to be used as a utility fixture i.e. as library
 */
public class DateFormatFixture extends SlimFixture {
    private String dateFormat;
    private TimeZone timezone;
    private String locale;
    private boolean timestampHasMilliseconds = true;

    public DateFormatFixture() {
        this(getDefaultFormat(), getDefaultTimeZone(), getDefaultLocale());
    }

    public DateFormatFixture(String dateformat) {
        this(dateformat, getDefaultTimeZone(), getDefaultLocale());
    }

    public DateFormatFixture(String dateformat, String timezone) {
        this(dateformat, timezone, getDefaultLocale());
    }

    public DateFormatFixture(String dateformat, String timezone, String locale) {
        setDateFormat(dateformat);
        setTimezone(timezone);
        setLocale(locale);
    }

    public void setDateFormat(String df) {
        dateFormat = df;
    }

    public void setTimezone(String newTimezone) {
        timezone = TimeZone.getTimeZone(newTimezone);
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public void timestampHasMilliseconds(boolean hasMillis) {
        timestampHasMilliseconds = hasMillis;
    }

    public String formatTimestamp(long timestamp) {
        return formatTimestamp(getConfiguredDateFormat(), timestamp);
    }

    public String formatTimestampAs(long timestamp, String dateFormat) {
        DateFormat sdf = getDateFormat(dateFormat);
        return formatTimestamp(sdf, timestamp);
    }

    public String formatDateAs(String date, String newFormat) {
        Date parsedDate = parseDate(date);
        DateFormat targetFormat = getDateFormat(newFormat);
        return targetFormat.format(parsedDate);
    }

    public long timestampForDate(String date) {
        Date parsedDate = parseDate(date);
        return timestampForDate(parsedDate);
    }

    public long timestampForDateAs(String date, String dateFormat) {
        SimpleDateFormat df = getDateFormat(dateFormat);
        Date parsedDate = parseDate(df, date);
        return timestampForDate(parsedDate);
    }

    protected long timestampForDate(Date parsedDate) {
        long timeStamp = parsedDate.getTime();
        if (!timestampHasMilliseconds) {
            timeStamp = timeStamp / 1000L;
        }
        return timeStamp;
    }

    protected String formatTimestamp(DateFormat sdf, long timestamp) {
        if (!timestampHasMilliseconds) {
            timestamp = timestamp * 1000L;
        }

        Date date = new Date(timestamp);
        return sdf.format(date);
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
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.forLanguageTag(locale));
        sdf.setTimeZone(timezone);
        return sdf;
    }

    protected static String getDefaultFormat() {
        return ((SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT)).toPattern();
    }

    protected static String getDefaultTimeZone() {
        return TimeZone.getDefault().getID();
    }

    protected static String getDefaultLocale() { return Locale.getDefault().getLanguage(); }
}

