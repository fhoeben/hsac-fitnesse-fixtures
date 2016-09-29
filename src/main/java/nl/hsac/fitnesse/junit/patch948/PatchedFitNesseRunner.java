package nl.hsac.fitnesse.junit.patch948;

import fitnesse.junit.FitNesseRunner;
import fitnesse.testrunner.MultipleTestsRunner;
import fitnesse.wiki.WikiPage;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

/**
 * Own subclass of FitNesseRunner to add test context to jUnit Descriptions.
 * Needed until FitNesse release containing https://github.com/unclebob/fitnesse/pull/948
 */
public class PatchedFitNesseRunner extends FitNesseRunner {
    public PatchedFitNesseRunner(Class<?> suiteClass) throws InitializationError {
        super(suiteClass);
    }

    @Override
    protected Description describeChild(WikiPage child) {
        Class<?> suiteClass = super.describeChild(child).getTestClass();
        return DescriptionHelper.createDescription(suiteClass, child);
    }

    @Override
    protected void addTestSystemListeners(RunNotifier notifier, MultipleTestsRunner testRunner, Class<?> suiteClass) {
        testRunner.addTestSystemListener(new RunNotifierResultsListener(notifier, suiteClass));
    }
}
