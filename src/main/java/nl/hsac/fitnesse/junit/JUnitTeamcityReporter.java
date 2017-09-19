package nl.hsac.fitnesse.junit;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.io.PrintStream;
import java.lang.management.ManagementFactory;

/**
 * JUnit RunListener so that TeamCity is updated with progress info while tests run.
 */
public class JUnitTeamcityReporter extends RunListener {
    private final PrintStream out;
    private final String flowId = ManagementFactory.getRuntimeMXBean().getName();
    private String currentTestClassName = null;

    public JUnitTeamcityReporter() {
        this(System.out);
    }

    public JUnitTeamcityReporter(final PrintStream out) {
        this.out = out;
        currentTestClassName = null;
    }

    @Override
    public void testStarted(Description description) {
        final String testClassName = getTestClassName(description);
        final String testName = getTestName(description);

        if (currentTestClassName == null || !currentTestClassName.equals(testClassName)) {
            testRunFinished(null);
            println("##teamcity[testSuiteStarted flowId='%s' name='%s']", flowId, testClassName);
            currentTestClassName = testClassName;
        }
        println("##teamcity[testStarted flowId='%s' name='%s' captureStandardOutput='true']", flowId, testName);
    }

    @Override
    public void testFinished(Description description) {
        final String testName = getTestName(description);

        println("##teamcity[testFinished flowId='%s' name='%s']", flowId, testName);
    }

    @Override
    public void testFailure(Failure failure) {
        if (failure.getTrace() != null && !failure.getTrace().isEmpty())
            print(failure.getTrace());
        println("##teamcity[testFailed flowId='%s' name='%s' message='%s' details='%s']",
                flowId,
                getTestName(failure.getDescription()),
                "failed",
                "");
        testFinished(failure.getDescription());
    }

    @Override
    public void testIgnored(Description description) {
        println("##teamcity[testIgnored flowId='%s' name='%s' message='%s']",
                flowId,
                getTestName(description),
                "");
    }

    @Override
    public void testRunFinished(Result result) {
        if (currentTestClassName != null) {
            println("##teamcity[testSuiteFinished flowId='%s' name='%s']", flowId, currentTestClassName);
        }
    }

    protected String getTestClassName(final Description description) {
        return description.getTestClass().getName();
    }

    protected String getTestName(final Description description) {
        return description.getMethodName();
    }

    private void println(String pattern, Object... args) {
        String msg = String.format(pattern, args);
        out.println(msg);
    }

    private void print(String msg) {
        out.print(msg);
    }
}
