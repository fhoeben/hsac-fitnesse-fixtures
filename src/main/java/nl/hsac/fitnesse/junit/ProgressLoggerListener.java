package nl.hsac.fitnesse.junit;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;

/**
 * JUnit RunListener so that build log is updated with progress info while tests run.
 */
public class ProgressLoggerListener extends RunListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProgressLoggerListener.class);
    private String currentTestClassName = null;
    private SimpleDateFormat formatter;
    private int totalChildCount;
    private int currentChild;


    public ProgressLoggerListener() {
        this.currentTestClassName = null;
        this.formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS ");
        this.totalChildCount = 0;
        this.currentChild = 0;
    }

    @Override
    public void testRunStarted(Description description) throws Exception {
        super.testRunStarted(description);
        totalChildCount = totalChildCount(description);
        currentChild = 0;
        LOGGER.info("testRun Started ('{}' tests)", totalChildCount);
    }

    private int totalChildCount(Description description) {
        int count = 0;
        if (description != null) {
            count = description.testCount();
        }
        return count;
    }

    @Override
    public void testStarted(Description description) throws Exception {
        final String testClassName = getTestClassName(description);
        final String testName = getTestName(description);

        if (currentTestClassName == null || !currentTestClassName.equals(testClassName)) {
            testRunFinished(null);
            LOGGER.info("testSuite Started '{}'", testClassName);
            currentTestClassName = testClassName;
        }
        currentChild++;
        LOGGER.info("test Started '{}}' ({} / {})", testName, currentChild, totalChildCount);
    }

    @Override
    public void testFinished(Description description) throws Exception {
        final String testName = getTestName(description);

        LOGGER.info("test Finished '{}'", testName);
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        LOGGER.error(String.format("test Failed '%s' message='%s' details='%s'",
                getTestName(failure.getDescription()),
                "failed"),
                failure.getException());
        testFinished(failure.getDescription());
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        LOGGER.info("test Ignored '{}'", getTestName(description));
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        if (currentTestClassName != null) {
            LOGGER.info("testSuite Finished '{}'", currentTestClassName);
        }
    }

    /**
     * @param description JUnit description of test executed
     * @return name to use in report
     */
    protected String getTestName(Description description) {
        return description.getMethodName();
    }

    protected String getTestClassName(final Description description) {
        return description.getTestClass().getName();
    }

    public void setFormatter(SimpleDateFormat formatter) {
        this.formatter = formatter;
    }
}
