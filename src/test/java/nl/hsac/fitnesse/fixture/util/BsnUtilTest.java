package nl.hsac.fitnesse.fixture.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BsnUtilTest {
    private final BsnUtil generator = new BsnUtil();

    @Test
    public void generateBsnTest() {
        for (int i = 0; i < 100; i++) {
            String result = generator.generateBsn();
            assertEquals("Got: " + result, 9, result.length());
            assertTrue("Got: " + result, generator.testBsn(result));
        }
    }

    @Test
    public void testBsnTest() {
        assertTrue(generator.testBsn("862574857"));
        assertTrue(generator.testBsn("541841300"));
        assertTrue(generator.testBsn("662582639"));
        assertTrue(generator.testBsn("554481789"));
        assertTrue(generator.testBsn("806173038"));
        assertTrue(generator.testBsn("976408028"));
        assertTrue(generator.testBsn("819280495"));
        assertTrue(generator.testBsn("909188774"));
        assertTrue(generator.testBsn("428206451"));
        assertTrue(generator.testBsn("054090088"));
        assertFalse(generator.testBsn("00123455"));
        assertFalse(generator.testBsn("054090089"));
        assertFalse(generator.testBsn("awefw"));
    }
}
