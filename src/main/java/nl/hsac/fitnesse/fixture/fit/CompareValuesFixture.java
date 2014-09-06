package nl.hsac.fitnesse.fixture.fit;



/**
  * Waits before continuing to the next fixture.
 */
public class CompareValuesFixture extends MapColumnFixture {
    public int waitTimeSeconds;
    private final static String VALUE1 = "value1";
    private final static String VALUE2 = "value2";

    public boolean isMatch() {
        String value1 = value1();
        String value2 = value2();
        return value1.equalsIgnoreCase(value2);
    }

    public boolean leftIsBiggerThanRight() {
        String value1 = value1();
        String value2 = value2();

        try {
            Integer value1Integer = Integer.valueOf(value1);
            Integer value2Integer = Integer.valueOf(value2);
            return value1Integer > value2Integer;
        } catch (NumberFormatException e) {
            return value1.compareTo(value2) > 0;
        }
    }

    public String value1() {
        return (String) getCurrentRowValues().get(VALUE1);
    }

    public String value2() {
        return (String) getCurrentRowValues().get(VALUE2);
    }

    public String asDelimiterSepList() {
        return value1() + getArraySeperator() + value2();
    }

    public String[] asArray() {
        return new String[] {value1(), value2()};
    }
}

