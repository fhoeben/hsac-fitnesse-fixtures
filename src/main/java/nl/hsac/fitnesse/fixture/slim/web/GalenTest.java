package nl.hsac.fitnesse.fixture.slim.web;

import com.galenframework.api.Galen;
import com.galenframework.reports.GalenTestInfo;
import com.galenframework.reports.HtmlReportBuilder;
import com.galenframework.reports.model.LayoutReport;
import com.galenframework.speclang2.pagespec.SectionFilter;
import com.galenframework.specs.Spec;
import com.galenframework.validation.ValidationError;
import com.galenframework.validation.ValidationObject;
import com.galenframework.validation.ValidationResult;
import nl.hsac.fitnesse.fixture.slim.SlimFixture;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static nl.hsac.fitnesse.fixture.util.FileUtil.ensureNoHtmlFiles;

/**
 * Fixture to check web page layout using Galen Framework.
 * @link http://galenframework.com
 */
public class GalenTest extends SlimFixture {
    private String reportBase = new File(filesDir, "galen-reports").getPath();
    private List<String> includedTags = Collections.emptyList();
    private List<String> excludedTags = Collections.emptyList();

    private List<GalenTestInfo> tests = new LinkedList<>();
    private LayoutReport layoutReport = new LayoutReport();

    public String layoutCheckUsing(String spec) throws IOException {
        String specPath = getFilePathFromWikiUrl(spec);
        GalenTestInfo test = getGalenTestInfo();

        checkLayout(specPath, test);

        return report();
    }

    protected void checkLayout(String specPath, GalenTestInfo test) throws IOException {
        String reportTitle = getReportTitle(specPath, includedTags(), excludedTags());
        SectionFilter sectionFilter = new SectionFilter(includedTags(), excludedTags());
        checkLayout(specPath, test, reportTitle, sectionFilter, new Properties(), null);
    }

    protected void checkLayout(String specPath, GalenTestInfo test, String reportTitle,
                               SectionFilter sectionFilter, Properties properties, Map<String, Object> jsVariables)
            throws IOException {
        layoutReport = Galen.checkLayout(getDriver(), specPath, sectionFilter, properties, jsVariables);

        // Adding layout report to the test report
        test.getReport().layout(layoutReport, reportTitle);
        tests.add(test);
    }

    protected String getReportTitle(String specPath, List<String> includedTags, List<String> excludedTags) {
        String tagsMsg = "";
        if (includedTags != null && !includedTags.isEmpty()) {
            tagsMsg += "; including " + includedTags;
        }
        if (excludedTags != null && !excludedTags.isEmpty()) {
            tagsMsg += "; excluding " + excludedTags;
        }
        return String.format("Layout check using: %s%s", specPath, tagsMsg);
    }

    protected GalenTestInfo getGalenTestInfo() {
        String name = String.format("FitNesse%s%s", getClass().getSimpleName(), tests.size());
        return GalenTestInfo.fromString(name);
    }

    public int layoutErrorCount() {
        return getLayoutReport().errors();
    }

    public Object layoutCheckMessages() {
        List<ValidationResult> errorResults = getLayoutReport().getValidationErrorResults();
        return formatResultsForWiki(errorResults);
    }

    protected Map<List<String>, Map<String, List<String>>> formatResultsForWiki(List<ValidationResult> errorResults) {
        Map<List<String>, Map<String, List<String>>> result = new LinkedHashMap<>();
        for (ValidationResult errorResult : errorResults) {
            List<String> key = formatValidationObjectsForWiki(errorResult.getValidationObjects());
            Map<String, List<String>> value = formatErrorForWiki(errorResult.getSpec(), errorResult.getError());

            if (result.containsKey(key)) {
                // add all current values to new value
                Map<String, List<String>> currentValue = result.get(key);
                addAllCurrentValues(value, currentValue);
            }
            result.put(key, value);
        }
        return result;
    }

    protected List<String> formatValidationObjectsForWiki(List<ValidationObject> validationObjects) {
        List<String> names = new ArrayList<>();
        for (ValidationObject error : validationObjects) {
            names.add(error.getName());
        }
        return names;
    }

    protected Map<String, List<String>> formatErrorForWiki(Spec spec, ValidationError error) {
        String key = error.isOnlyWarn() ? "warning" : "error";
        key += " on: " + spec.toText();
        Map<String, List<String>> messageMap = new LinkedHashMap<>();
        List<String> messages = error.getMessages();
        messageMap.put(key, new ArrayList<>(messages));
        return messageMap;
    }

    protected void addAllCurrentValues(Map<String, List<String>> value, Map<String, List<String>> currentValue) {
        for (Map.Entry<String, List<String>> currentEntries : currentValue.entrySet()) {
            String currentKey = currentEntries.getKey();
            List<String> currentValues = currentEntries.getValue();
            List<String> newValues = value.get(currentKey);
            if (newValues == null) {
                value.put(currentKey, currentValues);
            } else {
                newValues.addAll(currentValues);
            }
        }
    }

    public int layoutWarningCount() {
        return getLayoutReport().warnings();
    }

    public List<String> includedTags() {
        return includedTags;
    }

    public void setIncludedTags(List<String> includedTags) {
        this.includedTags = includedTags;
    }

    public List<String> excludedTags() {
        return excludedTags;
    }

    public void setExcludedTags(List<String> excludedTags) {
        this.excludedTags = excludedTags;
    }

    protected String report() throws IOException {
        generateHtmlReports();
        int testCount = tests.size();
        GalenTestInfo last = tests.get(testCount - 1);
        return createLinkToGalenReport(testCount, last);
    }

    protected String createLinkToGalenReport(int testCount, GalenTestInfo last) {
        String fileName = String.format("%s-%s.HTML", testCount, last.getName());
        String testPath = new File(getReportBase(), fileName).getPath();
        return String.format("<a href=\"%s\">report</a>", getWikiUrl(testPath));
    }

    protected void generateHtmlReports() throws IOException {
        String dir = getReportBase();
        new HtmlReportBuilder().build(tests, dir);
        ensureNoHtmlFiles(dir);
    }

    protected LayoutReport getLayoutReport() {
        return layoutReport;
    }

    protected String getReportBase() {
        return reportBase;
    }

    protected WebDriver getDriver() {
        return getEnvironment().getSeleniumHelper().driver();
    }
}
