package nl.hsac.fitnesse.fixture.util.selenium.by;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import java.util.function.Function;

/**
 * FirstElementBy which wraps its nested Bys in BestMatchBy, and has a preference for interactable elements.
 * If no interactable element is found it returns the first element matched (which was originally filtered out).
 */
public abstract class AbstractHeuristicBy extends FirstElementBy {
    /**
     * Creates new, using {@link IsInteractableFilter}.
     * (First By is separate so compiler will ensure at least one By is passed.)
     * @param firstNested first By to be wrapped.
     * @param extraNestedBys optional extra Bys to be wrapped.
     */
    protected AbstractHeuristicBy(By firstNested, By... extraNestedBys) {
        this(new IsInteractableFilter(), firstNested, extraNestedBys);
    }

    /**
     * Creates new.
     * (First By is separate so compiler will ensure at least one By is passed.)
     * @param postProcessor post processor to use.
     * @param firstNested first By to be wrapped.
     * @param extraNestedBys optional extra Bys to be wrapped.
     */
    protected AbstractHeuristicBy(IsInteractableFilter postProcessor, By firstNested, By... extraNestedBys) {
        super(postProcessor, wrapNested(firstNested, extraNestedBys));
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
        functions[0] = nestedFunction(firstNested);
        for (int i = 0; i < extraNestedBys.length; i++) {
            By nestedBy = extraNestedBys[i];
            functions[i + 1] = nestedFunction(nestedBy);
        }
        return functions;
    }

    private static Function<SearchContext, WebElement> nestedFunction(By nestedBy) {
        Function<SearchContext, WebElement> result;
        if (nestedBy instanceof SingleElementOrNullBy) {
            result = nestedBy::findElement;
        } else {
            // nested may not be needed, so we wait to create the nested BestMatchBy until it is needed
            result = (sc) -> new BestMatchBy(nestedBy).findElement(sc);
        }
        return result;
    }
}
