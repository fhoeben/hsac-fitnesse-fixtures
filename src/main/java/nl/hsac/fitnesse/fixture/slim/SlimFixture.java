package nl.hsac.fitnesse.fixture.slim;

import fitnesse.slim.fixtureInteraction.FixtureInteraction;
import fitnesse.slim.fixtureInteraction.InteractionAwareFixture;
import nl.hsac.fitnesse.fixture.Environment;
import nl.hsac.fitnesse.fixture.util.FileUtil;
import nl.hsac.fitnesse.slim.interaction.ExceptionHelper;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;

/**
 * Base class for Slim fixtures.
 */
public class SlimFixture  implements InteractionAwareFixture {
    private Environment environment = Environment.getInstance();
    private int repeatInterval = 100;
    private int repeatMaxCount = Integer.MAX_VALUE;
    private StopWatch repeatTimer = new StopWatch();
    private int repeatCount = 0;
    private long repeatTime = 0;
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

    // Polling
    public void setRepeatIntervalToMilliseconds(int milliseconds) {
        repeatInterval = milliseconds;
    }

    public long repeatInterval() {
        return repeatInterval;
    }

    public void repeatAtMostTimes(int maxCount) {
        repeatMaxCount = maxCount;
    }

    public int repeatAtMostTimes() {
        return repeatMaxCount;
    }

    public int repeatCount() {
        return repeatCount;
    }

    public long timeSpentRepeating() {
        return repeatTime;
    }

    protected boolean repeatUntil(RepeatCompletion repeat) {
        repeatTime = 0;
        repeatTimer.start();
        StopWatch loopTimer = new StopWatch();
        loopTimer.start();
        boolean result = repeat.isFinished();
        try {
            for (repeatCount = 0; !result && repeatCount < repeatMaxCount; repeatCount++) {
                int nextInterval = getNextInterval(loopTimer);
                waitMilliseconds(nextInterval);

                loopTimer.start();
                repeat.repeat();
                result = repeat.isFinished();
            }
        } finally {
            repeatTime = repeatTimer.getTime();
            repeatTimer.reset();
        }
        return result;
    }

    private int getNextInterval(StopWatch loopTimer) {
        int nextInterval;
        long loopTime = loopTimer.getTime();
        nextInterval = Math.max(0, ((int) (repeatInterval - loopTime)));
        loopTimer.reset();
        return nextInterval;
    }

    protected boolean repeatUntilNot(RepeatCompletion repeat) {
        return repeatUntil(new Negate(repeat));
    }

    /**
         * Interface to repeat a call until a condition is met.
         */
    public interface RepeatCompletion {
        /**
         * @return true if no more repeats are needed.
         */
        boolean isFinished();

        /**
         * Performs the action again.
         */
        void repeat();
    }

    /**
     * RepeatCompletion which negates the completion condition of another completion, but performs same action.
     */
    public class Negate implements RepeatCompletion {
        private final RepeatCompletion nested;

        /**
         * Creates new with same action, but the exact opposite completion condition.
         * @param nested completion whose #isFinished() will be negated.
         */
        public Negate(RepeatCompletion nested) {
            this.nested = nested;
        }

        @Override
        public boolean isFinished() {
            return !nested.isFinished();
        }

        @Override
        public void repeat() {
            nested.repeat();
        }
    }

    /**
     * RepeatCompletion using optional Runnable in repeat, calls function to determine whether it is completed.
     */
    public static class FunctionalCompletion implements RepeatCompletion {
        private Supplier<Boolean> isFinishedSupplier;
        private Runnable repeater;

        public FunctionalCompletion() {
        }

        public FunctionalCompletion(Supplier<Boolean> isFinishedSupplier) {
            this(isFinishedSupplier, null);
        }

        public FunctionalCompletion(Supplier<Boolean> isFinishedSupplier, Runnable repeater) {
            setIsFinishedSupplier(isFinishedSupplier);
            setRepeater(repeater);
        }

        @Override
        public boolean isFinished() {
            return Boolean.TRUE.equals(isFinishedSupplier.get());
        }

        @Override
        public void repeat() {
            if (repeater != null) {
                repeater.run();
            }
        }

        public void setIsFinishedSupplier(Supplier<Boolean> isFinishedSupplier) {
            this.isFinishedSupplier = isFinishedSupplier;
        }

        public void setRepeater(Runnable repeater) {
            this.repeater = repeater;
        }
    }
    // Polling

    /**
     * Creates a file using the supplied content.
     * @param dir directory to create file in.
     * @param fileName name for file.
     * @param content content to save.
     * @return link to created file.
     */
    protected String createFile(String dir, String fileName, byte[] content) {
        String baseName = FilenameUtils.getBaseName(fileName);
        String ext = FilenameUtils.getExtension(fileName);
        String downloadedFile = FileUtil.saveToFile(dir + baseName, ext, content);
        return linkToFile(downloadedFile);
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
     * Creates a wiki link to a file.
     * @param f file.
     * @return link referencing the file.
     */
    protected String linkToFile(String f) {
        return linkToFile(new File(f));
    }

    /**
     * Creates a wiki link to a file.
     * @param f file.
     * @return link referencing the file.
     */
    protected String linkToFile(File f) {
        String url = getWikiUrl(f.getAbsolutePath());
        if (url == null) {
            url = f.toURI().toString();
        }
        return String.format("<a href=\"%s\">%s</a>", url, f.getName());
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
