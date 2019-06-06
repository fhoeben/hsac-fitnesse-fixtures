package nl.hsac.fitnesse.fixture.slim;

import org.junit.Assert;
import org.junit.Test;

public class DateTimeCalculatorFixtureTest {

    @Test
    public void testUnitsAddedToDate() {
        DateTimeCalculatorFixture dtc = new DateTimeCalculatorFixture("dd-MM-yyyy");
        Assert.assertEquals("01-02-2017", dtc.addDaysTo(2, "30-01-2017"));
        Assert.assertEquals("01-02-2017", dtc.addMonthsTo(1, "01-01-2017"));
        Assert.assertEquals("01-01-2018", dtc.addYearsTo(1, "01-01-2017"));
    }

    @Test
    public void testUnitsAddedToTime() {
        DateTimeCalculatorFixture dtc = new DateTimeCalculatorFixture("HH:mm:ss");
        Assert.assertEquals("02:00:00", dtc.addHoursTo(2, "00:00:00"));
        Assert.assertEquals("13:37:00", dtc.addMinutesTo(37, "13:00:00"));
        Assert.assertEquals("15:23:03", dtc.addSecondsTo(5, "15:22:58"));
    }
}
