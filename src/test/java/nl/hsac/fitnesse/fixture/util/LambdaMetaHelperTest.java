package nl.hsac.fitnesse.fixture.util;

import org.junit.Test;
import org.openqa.selenium.Point;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class LambdaMetaHelperTest {
    private final LambdaMetaHelper helper = new LambdaMetaHelper();

    @Test
    public void testGetNoArg() throws Throwable {
        Supplier<LambdaMetaHelperTest> f = helper.getConstructor(getClass());
        assertNotNull(f.get());
        assertTrue(f.get() instanceof LambdaMetaHelperTest);
    }

    @Test
    public void testGetIntFromString() throws Throwable {
        Function<String, Integer> f = helper.getConstructor(Integer.class, String.class);
        assertEquals(Integer.valueOf(2), f.apply("2"));
    }

    @Test
    public void testTwoArg() throws Throwable {
        BiFunction<Integer, Integer, Point> f = helper.getConstructor(Point.class, int.class, int.class);
        Point r = f.apply(2, 2);
        assertEquals(new Point(2, 2), r);
    }


}
