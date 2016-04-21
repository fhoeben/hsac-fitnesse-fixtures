package nl.hsac.fitnesse.junit;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * JUnit RunListener so that build log is updated with progress info while tests run.
 */
public class ProgressLoggerListener extends RunListener {
    private final PrintStream out;
    private String currentTestClassName = null;
    private SimpleDateFormat formatter;
    private int totalChildCount;
    private int currentChild;

    public ProgressLoggerListener() {
        this(System.out);
    }

    public ProgressLoggerListener(final PrintStream out) {
        this.out = out;
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
        report("testRun Started ('%s' tests)", totalChildCount);
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
            report("testSuite Started '%s'", testClassName);
            currentTestClassName = testClassName;
        }
        if (testName == null
                || !(testName.endsWith(".SuiteSetUp")
                    || testName.endsWith(".SuiteTearDown"))) {
            // incrementing on setup/teardown causes current to become larger than total
            // that looks strangs
            currentChild++;
        }
        report("test Started '%s' (%s / %s)", testName, currentChild, totalChildCount);
    }

    @Override
    public void testFinished(Description description) throws Exception {
        final String testName = getTestName(description);

        report("test Finished '%s'\n", testName);
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        String trace = "";
        if (failure.getTrace() != null && !failure.getTrace().isEmpty()) {
            trace = failure.getTrace();
        }
        report("test Failed '%s' message='%s' details='%s'",
                getTestName(failure.getDescription()),
                "failed",
                trace);
        testFinished(failure.getDescription());
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        report("test Ignored '%s'", getTestName(description));
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        if (currentTestClassName != null) {
            report("testSuite Finished '%s'\n\n", currentTestClassName);
        }
    }

    protected void report(String pattern, Object... parameters) {
        out.print(formatter.format(new Date()));
        out.println(String.format(pattern, parameters));
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