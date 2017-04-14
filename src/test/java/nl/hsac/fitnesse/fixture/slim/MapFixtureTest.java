package nl.hsac.fitnesse.fixture.slim;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class MapFixtureTest {
    private MapFixture fixture;

    @Before
    public void setUp() {
        fixture = new MapFixture();
    }

    @Test
    public void nestedNamedByDefault() {
        assertTrue(fixture.expandsPeriodsInNamesToNestedMaps());
        fixture.expandPeriodsInNamesToNestedMaps(true);
        assertTrue(fixture.expandsPeriodsInNamesToNestedMaps());

        Object val = new Object();
        fixture.setValueFor(val, "parent.nested");

        Object parent = fixture.getCurrentValues().get("parent");
        assertTrue(parent instanceof Map);

        Object nested = ((Map) parent).get("nested");
        assertSame(val, nested);
    }

    @Test
    public void nestedNamedCanBeDisabled() {
        fixture.expandPeriodsInNamesToNestedMaps(false);
        assertFalse(fixture.expandsPeriodsInNamesToNestedMaps());

        Object val = new Object();
        fixture.setValueFor(val, "parent.nested");

        Map<String, Object> values = fixture.getCurrentValues();

        Object parent = values.get("parent");
        assertNull(parent);
        Object parentNested = values.get("parent.nested");
        assertSame(val, parentNested);

        fixture.expandPeriodsInNamesToNestedMaps(true);
        assertTrue(fixture.expandsPeriodsInNamesToNestedMaps());
    }
}
