package nl.hsac.fitnesse.fixture.util.selenium.by.relative;

import nl.hsac.fitnesse.fixture.slim.SlimFixtureException;

/**
 * Helper enum to call only existing relative methods from browser test
 */

public enum RelativeMethod {
    ABOVE("above"),
    BELOW("below"),
    NEAR("near"),
    TO_LEFT_OF("toLeftOf"),
    TO_RIGHT_OF("toRightOf");

    private String value;

    RelativeMethod(String value) {
        this.value = value;
    }

    public static RelativeMethod fromString(String s) {
        return java.util.Arrays.stream(RelativeMethod.values())
                .filter(m -> m.value.equalsIgnoreCase(s)).findFirst()
                .orElseThrow(() ->
                        new SlimFixtureException("Given value " + s + " is not found within RelativeMethod. Valid options: " + RelativeMethod.values()));
    }

    @Override
    public String toString() {
        return value;
    }
}
