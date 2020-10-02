package nl.hsac.fitnesse.fixture.slim;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SlimFixtureTest {
    @Test
    public void testCompareActualToExpected() {
        TestSlimFixture fixture = new TestSlimFixture();

        assertTrue(fixture.compareActualToExpected(null, null));
        assertTrue(fixture.compareActualToExpected("null", null));
        assertTrue(fixture.compareActualToExpected(1, 1));
        assertTrue(fixture.compareActualToExpected("a", "a"));
        assertTrue(fixture.compareActualToExpected("=~/\\d\\s\\w/", "1\ta"));

        assertFalse(fixture.compareActualToExpected(null, 1));
        assertFalse(fixture.compareActualToExpected("null", 1));
        assertFalse(fixture.compareActualToExpected("a", null));
        assertFalse(fixture.compareActualToExpected("a", "b"));
        assertFalse(fixture.compareActualToExpected("=~/\\d\\s\\w/", "1a"));
    }

    public class TestSlimFixture extends SlimFixture {}

}
