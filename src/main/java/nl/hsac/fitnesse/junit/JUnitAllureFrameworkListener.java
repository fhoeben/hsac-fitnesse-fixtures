//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package nl.hsac.fitnesse.junit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import ru.yandex.qatools.allure.Allure;
import ru.yandex.qatools.allure.config.AllureModelUtils;
import ru.yandex.qatools.allure.events.ClearStepStorageEvent;
import ru.yandex.qatools.allure.events.MakeAttachmentEvent;
import ru.yandex.qatools.allure.events.TestCaseFailureEvent;
import ru.yandex.qatools.allure.events.TestCaseFinishedEvent;
import ru.yandex.qatools.allure.events.TestCasePendingEvent;
import ru.yandex.qatools.allure.events.TestCaseStartedEvent;
import ru.yandex.qatools.allure.events.TestSuiteFinishedEvent;
import ru.yandex.qatools.allure.events.TestSuiteStartedEvent;
import ru.yandex.qatools.allure.model.Label;
import ru.yandex.qatools.allure.model.Status;
import ru.yandex.qatools.allure.model.Step;
import ru.yandex.qatools.allure.utils.AnnotationManager;

public class JUnitAllureFrameworkListener extends RunListener {
    private Allure lifecycle;
    private static final String FITNESSE_RESULTS_PATH = "target/fitnesse-results";
    private static final Pattern SCREENSHOT_PATTERN = Pattern.compile("href=\"([^\"]*.png)\"", 2);
    private static final Pattern PAGESOURCE_PATTERN = Pattern.compile("href=\"([^\"]*.html)\"", 2);
    private final Map<String, String> suites;

    public JUnitAllureFrameworkListener() {
        this.lifecycle = Allure.LIFECYCLE;
        this.suites = new HashMap();
    }

    public void testSuiteStarted(Description description) {
        String uid = this.generateSuiteUid(description.getDisplayName());
        String suiteName = System.getProperty("fitnesseSuiteToRun");
        if(null == suiteName) {
            suiteName = description.getDisplayName();
        }

        TestSuiteStartedEvent event = new TestSuiteStartedEvent(uid, suiteName);
        AnnotationManager am = new AnnotationManager(description.getAnnotations());
        am.update(event);
        event.withLabels(AllureModelUtils.createTestFrameworkLabel("JUnit"), new Label[0]);
        this.getLifecycle().fire(event);
    }

    public void testStarted(Description description) {
        TestCaseStartedEvent event = new TestCaseStartedEvent(this.getSuiteUid(description), description.getMethodName());
        AnnotationManager am = new AnnotationManager(description.getAnnotations());
        am.update(event);
        this.fireClearStepStorage();
        this.getLifecycle().fire(event);
    }

    public void testFailure(Failure failure) {
        if(failure.getDescription().isTest()) {
            this.fireTestCaseFailure(failure.getException());
            this.recordTestResult(failure.getDescription());
            Matcher screenshotMatcher = SCREENSHOT_PATTERN.matcher(failure.getException().getMessage());
            if(screenshotMatcher.find()) {
                String pageSourceMatcher = FITNESSE_RESULTS_PATH + screenshotMatcher.group(1);
                System.out.println("Screenshot at: " + pageSourceMatcher);
                MakeAttachmentEvent pageSourcePath = new MakeAttachmentEvent(this.attachScreenshotFailed(pageSourceMatcher), "Screenshot", "image/png");
                this.getLifecycle().fire(pageSourcePath);
            } else {
                System.out.println("No match for " + SCREENSHOT_PATTERN.toString() + " in " + failure.getException().getMessage());
            }

            Matcher pageSourceMatcher1 = PAGESOURCE_PATTERN.matcher(failure.getException().getMessage());
            if(pageSourceMatcher1.find()) {
                String pageSourcePath1 = FITNESSE_RESULTS_PATH + pageSourceMatcher1.group(1);
                System.out.println("PageSource at: " + pageSourcePath1);
                MakeAttachmentEvent pageSourceEvent = new MakeAttachmentEvent(this.attachScreenshotFailed(pageSourcePath1), "Page Source", "text/html");
                this.getLifecycle().fire(pageSourceEvent);
                pageSourceEvent.process(new Step());
            }
        } else {
            this.startFakeTestCase(failure.getDescription());
            this.fireTestCaseFailure(failure.getException());
            this.finishFakeTestCase();
        }

    }

    public void testAssumptionFailure(Failure failure) {
        this.testFailure(failure);
    }

    public void testIgnored(Description description) {
        this.startFakeTestCase(description);
        this.getLifecycle().fire((new TestCasePendingEvent()).withMessage(this.getIgnoredMessage(description)));
        this.finishFakeTestCase();
    }

    public void testFinished(Description description) {
        this.getLifecycle().fire(new TestCaseFinishedEvent());
    }

    public void testSuiteFinished(String uid) {
        this.getLifecycle().fire(new TestSuiteFinishedEvent(uid));
    }

    public void testRunFinished(Result result) {
        Iterator i$ = this.getSuites().values().iterator();

        while(i$.hasNext()) {
            String uid = (String)i$.next();
            this.testSuiteFinished(uid);
        }

    }

    public String generateSuiteUid(String suiteName) {
        String uid = UUID.randomUUID().toString();
        synchronized(this.getSuites()) {
            this.getSuites().put(suiteName, uid);
            return uid;
        }
    }

    public String getSuiteUid(Description description) {
        String suiteName = description.getClassName();
        if(!this.getSuites().containsKey(suiteName)) {
            Description suiteDescription = Description.createSuiteDescription(description.getTestClass());
            this.testSuiteStarted(suiteDescription);
        }

        return (String)this.getSuites().get(suiteName);
    }

    public String getIgnoredMessage(Description description) {
        Ignore ignore = (Ignore)description.getAnnotation(Ignore.class);
        return ignore != null && !ignore.value().isEmpty()?ignore.value():"Test ignored (without reason)!";
    }

    public void startFakeTestCase(Description description) {
        String uid = this.getSuiteUid(description);
        String name = description.isTest()?description.getMethodName():description.getClassName();
        TestCaseStartedEvent event = new TestCaseStartedEvent(uid, name);
        AnnotationManager am = new AnnotationManager(description.getAnnotations());
        am.update(event);
        this.fireClearStepStorage();
        this.getLifecycle().fire(event);
    }

    public void finishFakeTestCase() {
        this.getLifecycle().fire(new TestCaseFinishedEvent());
    }

    public void fireTestCaseFailure(Throwable throwable) {
        this.getLifecycle().fire((new TestCaseFailureEvent()).withThrowable(throwable));
        Status status = throwable instanceof AssertionError?Status.FAILED:Status.BROKEN;
        String statusStr = status.value();
        System.out.println("TC failed! Status: " + statusStr);
    }

    public void fireClearStepStorage() {
        this.getLifecycle().fire(new ClearStepStorageEvent());
    }

    public Allure getLifecycle() {
        return this.lifecycle;
    }

    public void setLifecycle(Allure lifecycle) {
        this.lifecycle = lifecycle;
    }

    public Map<String, String> getSuites() {
        return this.suites;
    }

    protected void recordTestResult(Description description) {
        this.testFinished(description);
    }

    public byte[] attachScreenshotFailed(String screenshotPath) {
        Path path = Paths.get(screenshotPath, new String[0]);

        byte[] data;
        try {
            data = Files.readAllBytes(path);
        } catch (IOException var5) {
            System.err.println("File not found: " + screenshotPath);
            data = null;
        }

        return data;
    }

    public byte[] attachPageSourceFailed(String pageSourcePath) {
        Path path = Paths.get(pageSourcePath, new String[0]);

        byte[] data;
        try {
            data = Files.readAllBytes(path);
        } catch (IOException var5) {
            System.err.println("File not found: " + pageSourcePath);
            data = null;
        }

        return data;
    }
}
