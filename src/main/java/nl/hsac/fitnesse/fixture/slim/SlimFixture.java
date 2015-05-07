package nl.hsac.fitnesse.fixture.slim;

import fitnesse.slim.fixtureInteraction.FixtureInteraction;
import nl.hsac.fitnesse.fixture.Environment;
import nl.hsac.fitnesse.slim.interaction.ExceptionHelper;
import nl.hsac.fitnesse.slim.interaction.InteractionAwareFixture;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for Slim fixtures.
 */
public class SlimFixture  implements InteractionAwareFixture {
    private static final Pattern PATTERN = Pattern.compile("<a href=\"(.*?)\">(.*?)</a>(.*)", Pattern.CASE_INSENSITIVE);
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
            throws InvocationTargetException, IllegalAccessException {
        return interaction.methodInvoke(method, this, arguments);
    }

    protected Throwable handleException(Method method, Object[] arguments, Throwable t) {
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
        String result = htmlLink;
        Matcher matcher = PATTERN.matcher(htmlLink);
        if (matcher.matches()) {
            result = matcher.group(1) + matcher.group(3);
        }
        return result;
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
     * @param rawValue value as received from Fitnesse.
     * @return rawValue if it was just text, cleaned version if it was not.
     */
    protected String cleanupValue(String rawValue) {
        String result = null;
        Matcher matcher = PATTERN.matcher(rawValue);
        if (matcher.matches()) {
            result = matcher.group(2) + matcher.group(3);
        } else {
            result = rawValue;
        }
        return result;
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
        String wikiUrl = null;
        if (filePath.startsWith(filesDir)) {
            String relativeFile = filePath.substring(filesDir.length());
            relativeFile = relativeFile.replace('\\', '/');
            wikiUrl = "files" + relativeFile;
        }
        return wikiUrl;
    }

    /**
     * Gets absolute path from wiki url, if file exists.
     * @param wikiUrl a relative path that can be used in wiki page, or any file path.
     * @return absolute path to the target of the url, if such a file exists; null if the target does not exist.
     */
    protected String getFilePathFromWikiUrl(String wikiUrl) {
        String url = getUrl(wikiUrl);
        File file;
        if (url.startsWith("files/")) {
            String relativeFile = url.substring("files".length());
            relativeFile = relativeFile.replace('/', File.separatorChar);
            String pathname = filesDir + relativeFile;
            file = new File(pathname);
        } else {
            file = new File(url);
        }
        return file.exists() ? file.getAbsolutePath() : url;
    }

}
