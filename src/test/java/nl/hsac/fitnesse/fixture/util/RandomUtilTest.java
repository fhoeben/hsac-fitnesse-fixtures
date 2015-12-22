package nl.hsac.fitnesse.fixture.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests RandomUtil.
 */
public class RandomUtilTest {
    private final RandomUtil util = new RandomUtil();

    /**
     * Tests int generation.
     */
    @Test
    public void testIntGenerate() {
        for (int i = 0; i < 1000; i++) {
            int result = util.random(10);
            assertTrue("Got: " + result, result < 10);
        }
    }

    /**
     * Tests lowercase string generation.
     */
    @Test
    public void testLowerCaseString() {
        for (int i = 0; i < 1000; i++) {
            String result = util.randomLower(10);
            assertEquals(10, result.length());
            assertEquals(result.toLowerCase(), result);
        }
    }

    /**
     * Tests lowercase string generation with variable length.
     */
    @Test
    public void testLowerCaseStringMax() {
        for (int i = 0; i < 1000; i++) {
            String result = util.randomLowerMaxLength(2, 10);
            assertTrue("Length was: " + result.length(), result.length() >= 2);
            assertTrue("Length was: " + result.length(), result.length() < 10);
            assertEquals(result.toLowerCase(), result);
        }
    }
}
