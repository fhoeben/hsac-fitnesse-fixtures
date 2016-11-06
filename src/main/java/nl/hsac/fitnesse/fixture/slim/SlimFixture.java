package nl.hsac.fitnesse.fixture.slim;

import fitnesse.slim.fixtureInteraction.FixtureInteraction;
import fitnesse.slim.fixtureInteraction.InteractionAwareFixture;
import nl.hsac.fitnesse.fixture.Environment;
import nl.hsac.fitnesse.slim.interaction.ExceptionHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Base class for Slim fixtures.
 */
public class SlimFixture  implements InteractionAwareFixture {
    private Environment environment = Environment.getInstance();
    protected final String filesDir = getEnvironment().getFitNesseFilesSectionDir();

    @Override
    public Object aroundSlimInvoke(FixtureInteraction interaction, Method method, Object... arguments)
            throws InvocationTargetException, IllegalAccessException {
        Object result;
        try {
            beforeInvoke(method, arguments);
            result = invoke(interaction, method, arguments);
        } catch (Throwable t) {
            Throwable realEx = ExceptionHelper.stripReflectionException(t);
            Throwable toThrow = handleException(method, arguments, realEx);
            if (toThrow instanceof RuntimeException) {
                throw (RuntimeException) toThrow;
            } else if (toThrow instanceof Error) {
                throw (Error) toThrow;
            }
            throw ExceptionHelper.wrapInReflectionException(toThrow);
        }
        result = afterCompletion(method, arguments, result);
        return result;
    }

    protected void beforeInvoke(Method method, Object[] arguments) {
    }

    protected Object invoke(FixtureInteraction interaction, Method method, Object[] arguments)
            throws Throwable {
        return interaction.methodInvoke(method, this, arguments);
    }

    protected Throwable handleException(Method method, Object[] arguments, Throwable t) {
        // convert any Fit, non-stacktrace, exception to our Slim equivalent
        if (t instanceof fit.exception.FitFailureException) {
            String m = t.getMessage();
            t = new SlimFixtureException(false, "<div>" + m + "</div>");
        }
        return t;
    }

    protected Object afterCompletion(Method method, Object[] arguments, Object result) {
        return result;
    }

    /**
     * @return environment to be used.
     */
    protected Environment getEnvironment() {
        return environment;
    }

    protected String getUrl(String htmlLink) {
        return getEnvironment().getHtmlCleaner().getUrl(htmlLink);
    }

    /**
     * Stores a (global) value so it can be accessed by other fixtures/pages.
     * @param symbolName name to store value under.
     * @param value value to store.
     */
    public void setGlobalValueTo(String symbolName, String value) {
        getEnvironment().setSymbol(symbolName, value);
    }

    /**
     * Retrieves a (global) value, which was previously stored using #setGlobalValueTo().
     * @param symbolName name value was stored under.
     */
    public String globalValue(String symbolName) {
        return getEnvironment().getSymbol(symbolName);
    }

    /**
     * Removes result of wiki formatting (for e.g. email addresses) if needed.
     * @param rawValue value as received from FitNesse.
     * @return rawValue if it was just text, cleaned version if it was not.
     */
    protected <T> T cleanupValue(T rawValue) {
        return getEnvironment().getHtmlCleaner().cleanupValue(rawValue);
    }

    public boolean waitSeconds(int i) {
        return waitMilliseconds(i * 1000);
    }

    public boolean waitMilliseconds(int i) {
        boolean result;
        try {
            Thread.sleep(i);
            result = true;
        } catch (InterruptedException e) {
            result = false;
        }
        return result;
    }

    /**
     * Converts a file path into a relative wiki path, if the path is insides the wiki's 'files' section.
     * @param filePath path to file.
     * @return relative URL pointing to the file (so a hyperlink to it can be created).
     */
    protected String getWikiUrl(String filePath) {
        return getEnvironment().getWikiUrl(filePath);
    }

    /**
     * Gets absolute path from wiki url, if file exists.
     * @param wikiUrl a relative path that can be used in wiki page, or any file path.
     * @return absolute path to the target of the url, if such a file exists; null if the target does not exist.
     */
    protected String getFilePathFromWikiUrl(String wikiUrl) {
        return getEnvironment().getFilePathFromWikiUrl(wikiUrl);
    }

}
