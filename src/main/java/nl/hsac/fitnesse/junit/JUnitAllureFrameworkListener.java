//
// JUnit listener for Allure Framework. Based on default ru.yandex.qatools.allure.junit.AllureRunListener
//

package nl.hsac.fitnesse.junit;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import fitnesse.junit.FitNesseRunner;
import fitnesse.wiki.WikiPage;
import org.apache.commons.io.FilenameUtils;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import ru.yandex.qatools.allure.Allure;
import ru.yandex.qatools.allure.config.AllureModelUtils;
import ru.yandex.qatools.allure.events.*;
import ru.yandex.qatools.allure.model.Label;
import ru.yandex.qatools.allure.utils.AnnotationManager;
import nl.hsac.fitnesse.junit.allure.AllureSetLabelsEvent;

public class JUnitAllureFrameworkListener extends RunListener {
    private Allure lifecycle;
    private static final String FITNESSE_RESULTS_PATH = "target/fitnesse-results/";
    private static final String SCREENSHOT_EXT = "png";
    private static final String PAGESOURCE_EXT = "html";
    private static final Pattern SCREENSHOT_PATTERN = Pattern.compile("href=\"([^\"]*." + SCREENSHOT_EXT + ")\"");
    private static final Pattern PAGESOURCE_PATTERN = Pattern.compile("href=\"([^\"]*." + PAGESOURCE_EXT + ")\"");
    private final HashMap suites;
    private List<WikiPage> pages;

    public JUnitAllureFrameworkListener() {
        this.lifecycle = Allure.LIFECYCLE;
        this.suites = new HashMap();
    }


    public void testSuiteStarted(Description description) {
        String uid = this.generateSuiteUid(description.getDisplayName());
        String suiteName = System.getProperty("fitnesseSuiteToRun");
        if(null == suiteName) {
            suiteName = description.getAnnotation(FitNesseRunner.Suite.class).value();
            }

        TestSuiteStartedEvent event = new TestSuiteStartedEvent(uid, suiteName);
        AnnotationManager am = new AnnotationManager(description.getAnnotations());
        am.update(event);
        event.withLabels(AllureModelUtils.createTestFrameworkLabel("FitNesse"));
        this.getLifecycle().fire(event);
    }

    public void testStarted(Description description) {
        TestCaseStartedEvent event = new TestCaseStartedEvent(this.getSuiteUid(description), description.getMethodName());
        AnnotationManager am = new AnnotationManager(description.getAnnotations());
        am.update(event);
        this.fireClearStepStorage();
        this.getLifecycle().fire(event);

        FitNessePageAnnotation pageAnn = description.getAnnotation(FitNessePageAnnotation.class);
        if(pageAnn != null) {
            WikiPage page = pageAnn.getWikiPage();
            page.getData().getProperties().get("Suites");
            String tagInfo = page.getData().getProperties().get("Suites");
            if(tagInfo != null) {
                createStories(tagInfo);
            }
        }
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

    public void testFinished(Description description) {
        String methodName = description.getMethodName();
        makeAttachment(fitnesseResult(methodName).getBytes(), "FitNesse Result page", "text/html");
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
                String type;
                String ext = FilenameUtils.getExtension(Paths.get(filePath).toString());
                if(ext.equalsIgnoreCase(SCREENSHOT_EXT)){
                    attName = "Page Screenshot";
                    type = "image/png";
                } else if(ext.equalsIgnoreCase(PAGESOURCE_EXT)){
                    attName = "Page Source";
                    type = "text/html";
                } else{
                    attName = "Attachment";
                    type = "text/html";
                }
                makeAttachment(fileToAttach(filePath), attName, type);
            }
        }
    }

    protected void makeAttachment(byte[] file, String attName, String type){
        MakeAttachmentEvent ev = new MakeAttachmentEvent(file, attName, type);
        this.getLifecycle().fire(ev);
    }

    protected byte[] fileToAttach(String filePath) {
        Path path = Paths.get(filePath);
        byte[] data;
        try {
            data = Files.readAllBytes(path);
        } catch (IOException var5) {
            System.err.println("file not found: " + path.toString());
            data = null;
        }
        return data;
    }

    protected String fitnesseResult(String test){
        String style = "width: 99%; height: 99%; overflow: auto; border: 0px;";
        String iFrame = String.format("<iframe src=\"/fitnesseResults/%s.html\" style=\"%s\">", test, style);
        String content = String.format("<html><head><title>FitNesse Report</title></head><body>%s</body>", iFrame);
        return content;
    }

    protected void createStories(String tagInfo){
        List<Label> labels = new ArrayList<>();
        String[] tags = tagInfo.split(",");
        for (String tag : tags) {
            tag = tag.trim();
            Label storyLabel = new Label();
            storyLabel.setName("story");
            storyLabel.setValue(tag);
            labels.add(storyLabel);
        }

        //For some reason, the host label no longer gets set when applying story labels..
        String hostName;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
            Label hostLabel = new Label();
            hostLabel.setName("host");
            hostLabel.setValue(hostName);
            labels.add(hostLabel);
        }
        catch(Exception ex) {
            System.err.println("Cannot determine hostname: " + ex);
        }
        AllureSetLabelsEvent event = new AllureSetLabelsEvent(labels);
        this.getLifecycle().fire(event);
    }
}
