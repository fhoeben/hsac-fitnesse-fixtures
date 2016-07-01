//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package nl.hsac.fitnesse.junit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import ru.yandex.qatools.allure.Allure;
import ru.yandex.qatools.allure.annotations.Attachment;
import ru.yandex.qatools.allure.config.AllureModelUtils;
import ru.yandex.qatools.allure.events.*;
import ru.yandex.qatools.allure.model.Status;
import ru.yandex.qatools.allure.utils.AnnotationManager;


public class JUnitAllureFrameworkListener extends RunListener {
    private Allure lifecycle;
    private static final String FITNESSE_RESULTS_PATH = "target/fitnesse-results/";
    private static final String SCREENSHOT_EXT = "png";
    private static final String PAGESOURCE_EXT = "html";
    private static final Pattern SCREENSHOT_PATTERN = Pattern.compile("href=\"([^\"]*." + SCREENSHOT_EXT + ")\"");
    private static final Pattern PAGESOURCE_PATTERN = Pattern.compile("href=\"([^\"]*." + PAGESOURCE_EXT + ")\"");
    private final HashMap suites;

    public JUnitAllureFrameworkListener() {
        this.lifecycle = Allure.LIFECYCLE;
        this.suites = new HashMap();
    }

    public void testSuiteStarted(Description description) {
       String suiteName = System.getProperty("fitnesseSuiteToRun");
        if(null == suiteName) {
            suiteName = description.getDisplayName();
            }
        String uid = this.generateSuiteUid(suiteName);

        TestSuiteStartedEvent event = new TestSuiteStartedEvent(uid, suiteName);
        AnnotationManager am = new AnnotationManager(description.getAnnotations());
        am.update(event);
        event.withLabels(AllureModelUtils.createTestFrameworkLabel("JUnit"));
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
            Throwable exception = failure.getException();

            List<Pattern> patterns = new ArrayList<>();
            patterns.add(SCREENSHOT_PATTERN);
            patterns.add(PAGESOURCE_PATTERN);
            processAttachments(exception, patterns);

            this.fireTestCaseFailure(exception);
            this.recordTestResult(failure.getDescription());


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

        for (String uid : this.getSuites().values()) {
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

        return this.getSuites().get(suiteName);
    }

    public String getIgnoredMessage(Description description) {
        Ignore ignore = description.getAnnotation(Ignore.class);
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

    protected void processAttachments(Throwable ex, List<Pattern> patterns){

        for(Pattern pattern : patterns){
            Matcher patternMatcher = pattern.matcher(ex.getMessage());
            if(patternMatcher.find()) {
                String filePath = FITNESSE_RESULTS_PATH + patternMatcher.group(1);
                String attName;
                String ext = FilenameUtils.getExtension(Paths.get(filePath).toString());
                if(ext.equalsIgnoreCase(SCREENSHOT_EXT)){
                    attName = "Page Screenshot";
                } else if(ext.equalsIgnoreCase(PAGESOURCE_EXT)){
                    attName = "Page Source";
                } else{
                    attName = "Attachment";
                }
                System.out.println("Attachment found at: " + filePath);
                attachFile(filePath, attName);
                //MakeAttachmentEvent event = new MakeAttachmentEvent(fileToAttach(filePath),"Attachment", "image/png");
                //this.getLifecycle().fire(event);
            } else {
                System.out.println("No match for " + pattern.toString() + " in " + ex.getMessage());
            }
        }


    }

    @Attachment(value = "{1}")
    protected byte[] attachFile(String filePath, String attName) {
        Path path = Paths.get(filePath);

        byte[] data;
        try {
            data = Files.readAllBytes(path);
        } catch (IOException var5) {
            System.err.println(attName + " not found: " + path.toString());
            data = null;
        }

        return data;
    }
}
