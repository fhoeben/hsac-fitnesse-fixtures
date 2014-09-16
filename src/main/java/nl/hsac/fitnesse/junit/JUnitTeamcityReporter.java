package nl.hsac.fitnesse.junit;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.io.PrintStream;

/**
 * JUnit RunListener so that TeamCity is updated with progress info while tests run.
 */
public class JUnitTeamcityReporter extends RunListener {
    private final PrintStream out;
    private String currentTestClassName = null;

    public JUnitTeamcityReporter() {
        this(System.out);
    }

    public JUnitTeamcityReporter(final PrintStream out) {
        this.out = out;
        this.currentTestClassName = null;
    }

    @Override
    public void testStarted(Description description) {
        final String testClassName = getTestClassName(description);
        final String testName = getTestName(description);

        if (currentTestClassName == null || !currentTestClassName.equals(testClassName)) {
            testRunFinished(null);
            this.out.println(String.format("##teamcity[testSuiteStarted name='%s']", testClassName));
            currentTestClassName = testClassName;
        }

        out.println(String.format("##teamcity[testStarted name='%s' captureStandardOutput='true']", testName));
    }

    @Override
    public void testFinished(Description description) {
        final String testName = getTestName(description);

        out.println(String.format("##teamcity[testFinished name='%s']", testName));
    }

    @Override
    public void testFailure(Failure failure) {
        if (failure.getTrace() != null && !failure.getTrace().isEmpty())
            out.print(failure.getTrace());
        out.println(String.format("##teamcity[testFailed name='%s' message='%s' details='%s']",
                getTestName(failure.getDescription()),
                "failed",
                ""));
        testFinished(failure.getDescription());
    }

    @Override
    public void testIgnored(Description description) {
        out.println(String.format("##teamcity[testIgnored name='%s' message='%s']",
                getTestName(description),
                ""));
    }

    @Override
    public void testRunFinished(Result result) {
        if (currentTestClassName != null) {
            out.println(String.format("##teamcity[testSuiteFinished name='%s']", currentTestClassName));
        }
    }

    protected String getTestClassName(final Description description) {
        return description.getTestClass().getName();
    }

    protected String getTestName(final Description description) {
        return description.getMethodName();
    }
}