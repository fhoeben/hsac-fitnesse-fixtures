package nl.hsac.fitnesse.fixture.util.selenium.by;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import java.util.function.Function;

/**
 * FirstElementBy which wraps its nested Bys in BestMatchBy, and has a preference for interactable elements.
 * If no interactable element is found it returns the first element matched (which was originally filtered out).
 */
public class HeuristicBy extends FirstElementBy {
    /**
     * Creates new.
     * (First By is separate so compiler will ensure at least one By is passed.)
     * @param firstNested first By to be wrapped.
     * @param extraNestedBys optional extra Bys to be wrapped.
     */
    public HeuristicBy(By firstNested, By... extraNestedBys) {
        super(wrapNested(firstNested, extraNestedBys));
        setPostProcessor(new IsInteractableFilter());
    }

    @Override
    public WebElement findElement(SearchContext context) {
        WebElement element = super.findElement(context);
        if (element == null) {
            // no interactable element found
            Function<WebElement, WebElement> postProcessor = getPostProcessor();
            if (postProcessor instanceof IsInteractableFilter) {
                element = ((IsInteractableFilter) postProcessor).getFirstFound();
            }
        }
        return element;
    }

    private static Function<SearchContext, WebElement>[] wrapNested(By firstNested, By[] extraNestedBys) {
        Function[] functions = new Function[extraNestedBys.length + 1];
        functions[0] = SingleElementOrNullBy.byToFunction(firstNested);
        for (int i = 0; i < extraNestedBys.length; i++) {
            BestMatchBy nestedBestMatch = new BestMatchBy(extraNestedBys[i]);
            functions[i + 1] = SingleElementOrNullBy.byToFunction(nestedBestMatch);
        }
        return functions;
    }
}