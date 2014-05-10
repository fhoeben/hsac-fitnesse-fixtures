package nl.hsac.fitnesse.fixture.util;

import nl.hsac.fitnesse.fixture.fit.SoapCallMapColumnFixture;

import java.util.ArrayList;
import java.util.List;

/**
 * Results from checking xpaths.
 */
public class XPathCheckResult {
    private String result = "OK";
    private List<Mismatch> mismatches = new ArrayList<Mismatch>();
    private String mismatchDetail;

    @Override
    public String toString() {
        String str = result;
        String mismatchDetails = getMismatchDetail();
        if (mismatchDetails != null) {
            str = mismatchDetails;
        }
        return str;
    }

    /**
     * Parse method to allow Fitnesse to determine expected values.
     * @param value expected value from cell.
     * @return object with supplied value as result.
     */
    public static XPathCheckResult parse(String value) {
        XPathCheckResult parsed = new XPathCheckResult();
        parsed.result = value;
        return parsed;
    }

    /**
     * Adds a mismatch to this result.
     * @param name name of value.
     * @param expected expected value.
     * @param actual value of XPath expression.
     */
    public void addMisMatch(String name, String expected, String actual) {
        result = "NOK";
        Mismatch mismatch = new Mismatch();
        mismatch.name = name;
        mismatch.expected = expected;
        mismatch.actual = actual;
        mismatches.add(mismatch);
    }

    /**
     * @return the mismatchDetail
     */
    public String getMismatchDetail() {
        String result = mismatchDetail;
        if (hasMismatch()) {
            StringBuilder builder = new StringBuilder(SoapCallMapColumnFixture.NO_ESCAPE_PREFIX + "NOK:\n<ul>\n");
            for (Mismatch mismatch : mismatches) {
                builder.append(" <li>");
                builder.append(mismatch.toString());
                builder.append("</li>\n");
            }
            builder.append("</ul>");
            result = builder.toString();
        }
        return result;
    }

    /**
     * @return whether result has one or more mismatches.
     */
    public boolean hasMismatch() {
        return !mismatches.isEmpty();
    }

    /**
     * @param aMismatchDetail the mismatchDetail to set
     */
    public void setMismatchDetail(String aMismatchDetail) {
        result = "NOK";
        mismatchDetail = aMismatchDetail;
    }
    
    @Override
    public boolean equals(Object anObject) {
        if (anObject == this) {
            return true;
        }
        if (anObject == null) {
            return false;
        }
        if (anObject instanceof XPathCheckResult) {
            return String.valueOf(((XPathCheckResult)anObject).getResult()).equalsIgnoreCase(result);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int ret = 0;
        if (result != null) {
            ret = result.hashCode();
        }
        return ret;
    }

    /**
     * @return the result
     */
    public String getResult() {
        return result;
    }

    private class Mismatch {
        public String name;
        public String expected;
        public String actual;

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(name);
            builder.append(": ");
            builder.append(expected);
            builder.append(" <> ");
            builder.append(actual);
            return builder.toString();
        }
    }
}
