package nl.hsac.fitnesse.fixture.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests XPathCheckResult.
 */
public class XPathCheckResultTest {

    @Test
    public void testHashCodeAndEquals() {
        XPathCheckResult checkResult = new XPathCheckResult();
        XPathCheckResult checkResult2 = new XPathCheckResult();

        resultsMatch(checkResult, checkResult2);
        
        checkResult.setMismatchDetail("NOK: a");
        checkResult2.addMisMatch("name", "expected", "actual");
        resultsMatch(checkResult, checkResult2);

        checkResult2 = new XPathCheckResult();
        resultsDontMatch(checkResult, checkResult2);
    }

    @Test
    public void testParse() {
        XPathCheckResult checkResult = new XPathCheckResult();
        XPathCheckResult checkResult2 = XPathCheckResult.parse("OK");
        resultsMatch(checkResult, checkResult2);
        
        checkResult.setMismatchDetail("NOK: a");
        checkResult2 = XPathCheckResult.parse("NOK");
        resultsMatch(checkResult, checkResult2);

        checkResult2 = XPathCheckResult.parse("OK");
        resultsDontMatch(checkResult, checkResult2);
    }

    private void resultsDontMatch(XPathCheckResult checkResult, XPathCheckResult checkResult2) {
        assertFalse(checkResult.equals(checkResult2));
        assertTrue(checkResult.hashCode() != checkResult2.hashCode());
    }

    private void resultsMatch(XPathCheckResult checkResult, XPathCheckResult checkResult2) {
        assertEquals(checkResult, checkResult2);
        assertEquals(checkResult.hashCode(), checkResult2.hashCode());
    }

}
