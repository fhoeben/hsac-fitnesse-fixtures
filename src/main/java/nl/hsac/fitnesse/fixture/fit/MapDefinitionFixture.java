package nl.hsac.fitnesse.fixture.fit;


import fit.Binding;
import fit.Parse;
import nl.hsac.fitnesse.fixture.Environment;

/**
 * Fixture that define a Map<String,Object> to be used in other fixtures.
 */
public class MapDefinitionFixture extends MapColumnFixture {
    public final static String NR_KEY = "nr";

    @Override
    public void reset() {
        super.reset();
        long nrValue = Environment.getInstance().getNextNr();
        String nr = Long.toString(nrValue);
        if (nr.length() > 10) {
            int origLength = nr.length();
            nr = nr.substring(origLength - 10);
        }
        getCurrentRowValues().put(NR_KEY, nr);
    }

    /**
     * @return map containing all fields of fixture as key.
     * @throws Exception if executed() throws.
     */
    private MapParameter parameter(String headerName) throws Exception {
        execute();
        String nr = (String) get(NR_KEY);
        MapParameter result = new MapParameter(headerName, nr);
        result.putAll(getCurrentRowValues());
        return result;
    }

    @Override
    protected Binding createBinding(int column, Parse heads) throws Throwable {
        Binding result;
        String header = heads.text();
        if (header.startsWith("=")) {
            result = new SaveMapParameterBinding(header.substring(1, header.length() - 1));
        } else {
            result = super.createBinding(column, heads);
        }
        return result;
    }

    private class SaveMapParameterBinding extends SaveBinding {
        private final String header;

        public SaveMapParameterBinding(String headerName) {
            header = headerName;
        }

        @Override
        protected MapParameter getValue() throws Exception {
            return parameter(header);
        }
    }

}
