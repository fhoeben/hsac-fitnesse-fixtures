package nl.hsac.fitnesse.fixture.fit;



/**
  * Compare values value1 and value2 in multiple ways.
 */
public class CompareValuesFixture extends MapColumnFixture {
    private final static String VALUE1 = "value1";
    private final static String VALUE2 = "value2";

    public boolean isMatch() {
        String value1 = value1();
        String value2 = value2();

        try {
            Double doubleValue1 = Double.valueOf(value1);
            Double doubleValue2 = Double.valueOf(value2);
            return doubleValue1.equals(doubleValue2);
        } catch (NumberFormatException e) {
            return value1.equalsIgnoreCase(value2);
        }
    }

    public boolean leftIsBiggerThanRight() {
        String value1 = value1();
        String value2 = value2();

        try {
            Double doubleValue1 = Double.valueOf(value1);
            Double doubleValue2 = Double.valueOf(value2);
            return doubleValue1 > doubleValue2;
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

