package nl.hsac.fitnesse.fixture.util.selenium.by;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * FirstElementBy which if no interactable element is found returns the first element matched
 * (which was originally filtered out).
 * @param <T> type of element to return.
 */
public class HeuristicBy<T extends WebElement> extends FirstElementBy<T> {
    /**
     * Creates new, using {@link IsInteractableFilter}.
     * (First By is separate so compiler will ensure at least one By is passed.)
     * @param firstNested first By to be wrapped.
     * @param extraNestedBys optional extra Bys to be wrapped.
     */
    public HeuristicBy(By firstNested, By... extraNestedBys) {
        this(new IsInteractableFilter(), firstNested, extraNestedBys);
    }

    /**
     * Creates new.
     * (First By is separate so compiler will ensure at least one By is passed.)
     * @param postProcessor post processor to use.
     * @param firstNested first By to be wrapped.
     * @param extraNestedBys optional extra Bys to be wrapped.
     */
    public HeuristicBy(Function<? super T, ? extends T> postProcessor, By firstNested, By... extraNestedBys) {
        super(postProcessor, firstNested, extraNestedBys);
    }

    @Override
    public T findElement(SearchContext context) {
        T element = super.findElement(context);
        if (element == null) {
            // no interactable element found
            Object postProcessor = getPostProcessor();
            if (postProcessor instanceof Supplier) {
                Supplier supplier = (Supplier) postProcessor;
                element = (T) supplier.get();
            }
        }
        return element;
    }
}
