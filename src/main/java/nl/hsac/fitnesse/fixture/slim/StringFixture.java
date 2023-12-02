package nl.hsac.fitnesse.fixture.slim;

import nl.hsac.fitnesse.fixture.util.LineEndingHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fixture with helper methods to allow more manipulation to be done in the Wiki.
 * Most likely use is as a library to add its methods to the methods available via another fixture in a script table.
 */
public class StringFixture extends SlimFixture {
    /**
     * Returns value.
     * @param value value to return
     * @return value
     */
    public String valueOf(String value) {
        return value;
    }

    /**
     * NOTE: Duplicate implementation of method 'valueOf' to enable use in scripts started with another fixture containing the same method name (eg. BrowserTest)
     * Returns value.
     * @param value value to return
     */
    public String getValueOf(String value) {
        return value;
    }

    /**
     * Determines length of string.
     * @param value value to determine length of
     * @return length of value
     */
    public int lengthOf(String value) {
        int length = 0;
        if (value != null) {
            length = value.length();
        }
        return length;
    }

    /**
     * Checks if an input string is empty: null or length() = 0.
     *
     * @param value value to check.
     * @return {@code true} if empty, or {@code false} otherwise.
     */
    public boolean isEmpty(String value) { return StringUtils.isEmpty(value); }

    /**
     * Checks if a string is not empty: not null and length() != 0.
     *
     * @param value value to check.
     * @return {@code true} if filled (not empty), {@code false} otherwise.
     */
    public boolean isNotEmpty(String value) { return !isEmpty(value); }

    /**
     * Checks if a value meets the symbol naming convention ('$xyz').
     * This indicates an undefined FitNesse symbol.
     * http://fitnesse.org/FitNesse.SuiteAcceptanceTests.SuiteSlimTests.SlimSymbols.NamingConvention
     *
     * @param value value to check.
     * @return {@code true} if undefined (value is symbol name-like), {@code false} otherwise.
     */
    public boolean isUndefined(String value) {
        boolean result = false;
        if (getMatcher("\\$[a-zA-Z][a-zA-Z0-9_]*", value).matches()) {
            result = true;
        }
        return result;
    }

    /**
     * Checks if a value is empty or has a value that meets the symbol naming convention ('$xyz').
     * The latter indicates an undefined FitNesse symbol.
     *
     * @param value value to check.
     * @return {@code true} if empty or symbol name-like, {@code false} otherwise.
     */
    public boolean isEmptyOrUndefined(String value) {
        boolean result = false;
        if (isEmpty(value) || isUndefined(value)) {
            result = true;
        }
        return result;
    }

    /**
     * <p>Compares two Strings, returning <code>false</code> if they are equal.</p>
     *
     * <p><code>null</code>s are handled without exceptions. Two <code>null</code>
     * references are considered to be equal. The comparison is case sensitive.</p>
     *
     * @see StringUtils#equals(CharSequence, CharSequence)
     * @param value1  the first String, may be null
     * @param value2  the second String, may be null
     * @return <code>false</code> if the Strings are equal, or both <code>null</code>
     */
    public boolean valueDiffersFrom(String value1, String value2) {
        return !valueEquals(value1, value2);
    }

    /**
     * <p>Compares two Strings, returning <code>true</code> if they are equal.</p>
     *
     * <p><code>null</code>s are handled without exceptions. Two <code>null</code>
     * references are considered to be equal. The comparison is case sensitive.</p>
     *
     * @see StringUtils#equals(CharSequence, CharSequence)
     * @param value1  the first String, may be null
     * @param value2  the second String, may be null
     * @return <code>true</code> if the Strings are equal, or both <code>null</code>
     */
    public boolean valueEquals(String value1, String value2) {
        return StringUtils.equals(value1, value2);
    }

    /**
     * Checks whether values contains a specific sub string.
     * @param value value to find substring in.
     * @param expectedSubstring text that is expected to occur in value.
     * @return true if value contained the expected substring.
     */
    public boolean textContains(String value, String expectedSubstring) {
        boolean result = false;
        if (value != null) {
            result = value.contains(expectedSubstring);
        }
        return result;
    }

    /**
     * Converts the value to upper case.
     * @param value value to put in upper case.
     * @return value in capital letters.
     */
    public String convertToUpperCase(String value) {
        String result = null;
        if (value != null) {
            result = value.toUpperCase();
        }
        return result;
    }

    /**
     * Converts the value to lower case.
     * @param value value to put in lower case.
     * @return value with all capital letters replaced by normal letters.
     */
    public String convertToLowerCase(String value) {
        String result = null;
        if (value != null) {
            result = value.toLowerCase();
        }
        return result;
    }

    /**
     * Capitalises the first character of a string.
     * @param value value to capitalise.
     * @return capitalised value.
     */
    public String capitalise (String value) {
        String result = null;
        if (value != null) {
            result = StringUtils.capitalize(value);
        }
        return result;
    }

    /**
     * Determines integer value of String (so relative checks can be done).
     * @param value string to convert to integer.
     * @return integer value.
     */
    public Integer convertToInt(String value) {
        Integer result = null;
        if (value != null) {
            result = Integer.valueOf(value);
        }
        return result;
    }

    /**
     * Determines double value of String (so relative checks can be done).
     * @param value string to convert to double.
     * @return double value.
     */
    public Double convertToDouble(String value) {
        Double result = null;
        if (value != null) {
            result = Double.valueOf(value);
        }
        return result;
    }

    /**
     * Removes all (sequences of) whitespace.
     * @param value value to clean up.
     * @return value without whitespace.
     */
    public String removeWhitespace(String value) {
        String result = null;
        if (value != null) {
            result = replaceAllInWith("\\s+", value, "");
        }
        return result;
    }

    /**
     * Trims value and replaces all (sequences of) whitespace to a single space.
     * @param value value to clean up.
     * @return trimmed value with all whitespace converted to single space.
     */
    public String normalizeWhitespace(String value) {
        String result = null;
        if (value != null) {
            value = value.trim();
            result = replaceAllInWith("\\s+", value, " ");
        }
        return result;
    }

    /**
     * Replaces all occurrences of the regular expression in the value with the replacement value.
     * @param regEx regular expression to match.
     * @param value value to replace in.
     * @param replace replacement pattern.
     * @return result.
     */
    public String replaceAllInWith(String regEx, String value, String replace) {
        String result = null;
        if (value != null) {
            if (replace == null) {
                // empty cell in table is sent as null
                replace = "";
            }
            result = getMatcher(regEx, value).replaceAll(replace);
        }
        return result;
    }

    /**
     * Extracts a whole number for a string using a regular expression.
     * @param value input string.
     * @param regEx regular expression to match against value.
     * @param groupIndex index of group in regular expression containing the number.
     * @return extracted number.
     */
    public Integer extractIntFromUsingGroup(String value, String regEx, int groupIndex) {
        Integer result = null;
        if (value != null) {
            Matcher matcher = getMatcher(regEx, value);
            if (matcher.matches()) {
                String intStr = matcher.group(groupIndex);
                result = convertToInt(intStr);
            }
        }
        return result;
    }

    protected Matcher getMatcher(String regEx, String value) {
        return Pattern.compile(regEx, Pattern.DOTALL).matcher(value);
    }

    public String convertLineEndingsToWindows(String input) {
        return getEnvironment().getLineEndingHelper().convertEndingsTo(input, LineEndingHelper.WIN_LINE_ENDING);
    }

    public String convertLineEndingsToUnix(String input) {
        return getEnvironment().getLineEndingHelper().convertEndingsTo(input, LineEndingHelper.UNIX_LINE_ENDING);
    }

    /**
     * Returns a null value.
     *
     * @return null value.
     */
    public String setNullValue() { return null;}
}
