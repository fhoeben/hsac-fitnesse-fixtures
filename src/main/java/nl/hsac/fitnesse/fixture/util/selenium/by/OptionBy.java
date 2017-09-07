package nl.hsac.fitnesse.fixture.util.selenium.by;

/**
 * By to find option for select element.
 */
public class OptionBy extends HeuristicBy {
    private final String text;

    public OptionBy(String optionValue) {
        super(new XPathBy(".//option[normalized(text()) = '%s']", optionValue),
                new XPathBy(".//option[contains(normalized(text()), '%s')]", optionValue));
        text = optionValue;
    }

    @Override
    public String toString() {
        return super.toString() + ":" + text;
    }
}
