package nl.hsac.fitnesse.fixture.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static nl.hsac.fitnesse.fixture.util.FirstNonNullHelper.firstNonNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FirstNonNullHelperTest {
    @Test
    public void firstNonNullSupplier() {
        assertEquals(this, firstNonNull(() -> this));

        assertEquals(this, firstNonNull(() -> null, () -> this));
    }

    @Test
    public void firstNonNullCollection() {
        assertEquals(toString(), firstNonNull(x -> x.toString(), Collections.singleton(this)));

        assertEquals(toString(), firstNonNull(x -> x == null? null: x.toString(), Arrays.asList(null, this)));
    }

    @Test
    public void firstNonNullCollectionSupplier() {
        assertEquals(toString(), firstNonNull(x -> x.toString(), () -> Collections.singleton(this)));

        assertEquals(toString(), firstNonNull(x -> x == null? null: x.toString(), () -> Arrays.asList(null, this)));

        assertEquals(toString(), firstNonNull(x -> x == null? null: x.toString(),
                () -> null,
                () -> Collections.emptyList(),
                () -> Arrays.asList(null, null),
                () -> Arrays.asList(null, this)));
    }

    @Test
    public void firstNonNullNullArray() {
        assertNull(firstNonNull(x -> x++, (Integer[]) null));
    }

    @Test
    public void firstNonNullEmptyArray() {
        assertNull(firstNonNull(x -> x++, new Integer[0]));
    }

    @Test
    public void firstNonNullNullCollection() {
        assertNull(firstNonNull(x -> x++, (Collection<Integer>) null));
    }
}
