package nl.hsac.fitnesse.fixture.slim;

/**
 * Utility fixture to abort a test run.
 */
public class StopTestFixture {
    /**
     * @param actual actual value.
     * @param expected expected value.
     * @return true if actual value equals expected value.
     * @throws StopTestException if actual does not match expected.
     */
    public boolean stopTestIfIs(Object actual, Object expected) {
        if ((expected == null && actual == null)
                || (expected != null && expected.equals(actual))
                || areEqualAsString(actual, expected)) {
            throw new StopTestException(false, "Stopping test. Value is: " + actual);
        }
        return true;
    }

    /**
     * @param actual actual value.
     * @param notExpected value which is not allowed.
     * @return true if actual value does not equal expected value.
     * @throws StopTestException if actual does equal notExpected.
     */
    public boolean stopTestIfIsNot(Object actual, Object notExpected) {
        if ((notExpected == null && actual != null)
                || (notExpected != null && !notExpected.equals(actual))) {
            // see whether they are the same in String format
            if (!areEqualAsString(actual, notExpected)) {
                throw new StopTestException(false, "Stopping test. Value is: " + actual);
            }
        }
        return true;
    }

    private boolean areEqualAsString(Object left, Object right) {
        String rightAsString = String.valueOf(right);
        String leftAsString = String.valueOf(left);
        return rightAsString.equals(leftAsString);
    }
}
