package nl.hsac.fitnesse.fixture.util.selenium;

import org.openqa.selenium.SearchContext;

/**
 * Exception thrown when current context in {@link SeleniumHelper} is no longer usable.
 */
public class StaleContextException extends RuntimeException {
    private final SearchContext searchContext;

    public StaleContextException(SearchContext context) {
        super("Search context is 'stale' and cannot be used to find elements. Context is: " + context);
        searchContext = context;
    }

    /**
     * @return 'stale' context encountered.
     */
    public SearchContext getSearchContext() {
        return searchContext;
    }
}
