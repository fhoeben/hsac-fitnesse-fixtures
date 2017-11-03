package nl.hsac.fitnesse.fixture.slim;

import nl.hsac.fitnesse.fixture.util.MapHelper;

import java.util.Map;

/**
 * Slim fixture base class allowing values to be set to a Map.
 */
public class SlimFixtureWithMapHelper extends SlimFixture {
    private MapHelper mapHelper;

    public SlimFixtureWithMapHelper() {
        expandPeriodsInNamesToNestedMaps(true);
    }

    public void expandPeriodsInNamesToNestedMaps(boolean expand) {
        MapHelper newMapHelper;
        if (expand) {
            newMapHelper = getDefaultMapHelper();
        } else {
            newMapHelper = new NoOpMapHelper();
        }
        setMapHelper(newMapHelper);
    }

    public boolean expandsPeriodsInNamesToNestedMaps() {
        return !(getMapHelper() instanceof NoOpMapHelper);
    }

    /**
     * @return helper to assist getting/setting (nested) values in a map.
     */
    public MapHelper getMapHelper() {
        return mapHelper;
    }

    /**
     * @param mapHelper helper to assist getting/setting (nested) values in a map.
     */
    public void setMapHelper(MapHelper mapHelper) {
        this.mapHelper = mapHelper;
    }

    protected MapHelper getDefaultMapHelper() {
        return getEnvironment().getMapHelper();
    }

    /**
     * Map helper that does not interpret periods in names to mean nested maps.
     */
    private static class NoOpMapHelper extends MapHelper {
        @Override
        public void setValueForIn(Object value, String name, Map<String, Object> map) {
            map.put(name, value);
        }
    }
}
