package nl.hsac.fitnesse.fixture.fit;

import fit.ColumnFixture;
import fit.Parse;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * ColumnFixture with some enhancements.
 */
public class OurColumnFixture extends ColumnFixture {
    /** Prefix for method return String if value should not be HTML escaped (used to provide more detail in HTML format).*/
    public static final String NO_ESCAPE_PREFIX = "@@NO_ESCAPE@@";

    @Override
    public void execute() {
        calculateDerivedValues();
    }

    /**
     * Adds any values to currentRowValues that are not explicitly set in Wiki
     * table, but should instead be derived from the values set.
     */
    protected void calculateDerivedValues() {
    }

    /**
     * Gets fixture parameter (i.e. extra column in header row).
     * Please note this can not be called from a constructor, as the parameters will not have been initialized yet!
     * @param index index (zero based) to get value from.
     * @return null if there was no parameter at index, or it was no integer.
     */
    protected Integer parseIntArg(int index) {
        return parseIntArg(index, null);
    }

    /**
     * Gets fixture parameter (i.e. extra column in header row).
     * Please note this can not be called from a constructor, as the parameters will not have been initialized yet!
     * @param index index (zero based) to get value from.
     * @param defaultValue value to use if parameter is not present or was no int.
     * @return parameter value, if present and an integer, defaultValue otherwise.
     */
    protected Integer parseIntArg(int index, Integer defaultValue) {
        Integer result = defaultValue;
        try {
            String argValue = getStringArg(index);
            if (argValue != null && !"".equals(argValue)) {
                result = Integer.valueOf(argValue);
            }
        } catch (NumberFormatException e) {
            // don't show just ignore, we will return null
        }
        return result;
    }

    /**
     * Gets fixture parameter (i.e. extra column in header row).
     * Please note this can not be called from a constructor, as the parameters will not have been initialized yet!
     * @param index index (zero based) to get value from.
     * @return null if there was no parameter at index.
     */
    protected String getStringArg(int index) {
        return getStringArg(index, null);
    }

    /**
     * Gets fixture parameter (i.e. extra column in header row).
     * Please note this can not be called from a constructor, as the parameters will not have been initialized yet!
     * @param index index (zero based) to get value from.
     * @param defaultValue value to use if parameter is not present.
     * @return parameter value, if present, defaultValue otherwise.
     */
    protected String getStringArg(int index, String defaultValue) {
        String result = defaultValue;
        String[] arg = getArgs();
        if (arg != null) {
            if (arg.length > index) {
                result = arg[index];
            }
        }
        return result;
    }

    /**
     * @return current time, can be useful to trace a row's execution to log files of the system under test.
     */
    public String timeStamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(getTimeStampPattern());
        return dateFormat.format(Calendar.getInstance().getTime());
    }

    /**
     * @return pattern used to format current time by #timeStamp().
     */
    protected String getTimeStampPattern() {
        return "yyyy-MM-dd HH:mm:ss";
    }

    @Override
    public void wrong(Parse cell, String actual) {
        // if prefix is used we don't escape the actual value
        if (actual.startsWith(NO_ESCAPE_PREFIX)) {
            wrong(cell);

            String actualNoPrefix = actual.substring(NO_ESCAPE_PREFIX.length());
            cell.addToBody(label("expected") + "<hr>" + actualNoPrefix + label("actual"));
        } else {
            super.wrong(cell, actual);
        }
    }
}
