package nl.hsac.fitnesse.fixture.slim;

import org.apache.commons.lang.StringUtils;

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
     * <p>Compares two Strings, returning <code>false</code> if they are equal.</p>
     *
     * <p><code>null</code>s are handled without exceptions. Two <code>null</code>
     * references are considered to be equal. The comparison is case sensitive.</p>
     *
     * @see org.apache.commons.lang.StringUtils#equals(String, String)
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
     * @see org.apache.commons.lang.StringUtils#equals(String, String)
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
}
