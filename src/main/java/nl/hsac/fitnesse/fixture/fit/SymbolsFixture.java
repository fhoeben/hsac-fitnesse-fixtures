package nl.hsac.fitnesse.fixture.fit;

import fit.Fixture;
import fitnesse.fixtures.TableFixture;
import nl.hsac.fitnesse.fixture.Environment;

/**
 * Fixture to set Fitnesse Symbols so that other fixtures can use them.
 */
public class SymbolsFixture extends TableFixture {

    @Override
    protected void doStaticTable(int rowCount) {
        if ("key".equalsIgnoreCase(getText(0, 0))) {
            right(0, 0);
        }
        if ("value".equalsIgnoreCase(getText(0, 1))) {
            right(0, 1);
        }
        for(int i = 1; i < rowCount; i++) {
            String key = getText(i, 0);
            String value = getText(i, 1);
            Fixture.setSymbol(key, value);
            Environment.getInstance().setSymbol(key, value);
        }
    }

}
