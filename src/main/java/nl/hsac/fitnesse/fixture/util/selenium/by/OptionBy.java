package nl.hsac.fitnesse.fixture.util.selenium.by;

/**
 * By to find option for select element.
 */
public class OptionBy extends HeuristicBy {
    private final String text;

    public OptionBy(String optionValue) {
        super("".equals(optionValue) ? ConstantBy.getEmptyOptionBy() : ConstantBy.nothing(),
                new XPathBy(".//option/text()[normalized(.) = '%s']/..", optionValue),
                "".equals(optionValue) ? ConstantBy.nothing() : new XPathBy(".//option/text()[contains(normalized(.), '%s')]/..", optionValue));
        text = optionValue;
    }

    @Override
    public String toString() {
        return super.toString() + ":" + text;
    }
}
