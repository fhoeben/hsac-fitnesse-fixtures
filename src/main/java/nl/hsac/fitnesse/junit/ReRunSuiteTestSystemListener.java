package nl.hsac.fitnesse.junit;

import fitnesse.testsystems.Assertion;
import fitnesse.testsystems.ExceptionResult;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.TestSystemListener;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Creates a wiki page with all pages that either failed or threw exception
 */
public class ReRunSuiteTestSystemListener implements TestSystemListener, Closeable {
    private final File wikiFile;
    private final PrintWriter pw;
    private boolean hasContent = false;

    public ReRunSuiteTestSystemListener(String path) throws IOException {
        wikiFile = new File(path);
        if (!wikiFile.getParentFile().exists()) {
            wikiFile.getParentFile().mkdirs();
        } else if (wikiFile.exists()) {
            wikiFile.delete();
        }
        pw = new PrintWriter(wikiFile, "utf-8");
    }

    @Override
    public void testComplete(TestPage testPage, TestSummary testSummary) {
        if (testSummary.getExceptions() > 0) {
            recordFailure(testPage);
        } else if (testSummary.getWrong() > 0) {
            recordFailure(testPage);
        }
    }

    @Override
    public void close() {
        pw.close();
        // no content -> remove file
        if (!hasContent && wikiFile.exists()) {
            wikiFile.delete();
        }
    }

    @Override
    public void testSystemStarted(TestSystem testSystem) {
        // no op
    }

    @Override
    public void testStarted(TestPage testPage) {
        // no op
    }

    @Override
    public void testOutputChunk(String s) {
        // no op
    }

    @Override
    public void testAssertionVerified(Assertion assertion, TestResult testResult) {
        // no op
    }

    @Override
    public void testExceptionOccurred(Assertion assertion, ExceptionResult exceptionResult) {
        // no op
    }

    @Override
    public void testSystemStopped(TestSystem testSystem, Throwable throwable) {
        // no op
    }

    protected void recordFailure(TestPage testPage) {
        String testPageName = testPage.getName();
        if (!"SuiteSetUp".equals(testPageName)
                && !"SuiteTearDown".equals(testPageName)) {
            if (!hasContent) {
                pw.append(
                        "---\n" +
                                "Suite\n" +
                                "---\n" +
                                "\n");
                hasContent = true;
            }
            String pagePath = testPage.getFullPath();
            pw.append("!see [[");
            pw.append(pagePath);
            pw.append("][.");
            pw.append(pagePath);
            pw.append("]]\n");
            pw.flush();
        }
    }
}
