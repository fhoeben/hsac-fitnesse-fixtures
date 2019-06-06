package nl.hsac.fitnesse.fixture.slim.web;

import com.galenframework.api.Galen;
import com.galenframework.reports.GalenTestInfo;
import com.galenframework.reports.HtmlReportBuilder;
import com.galenframework.reports.TestReport;
import com.galenframework.reports.TestStatistic;
import com.galenframework.reports.model.LayoutReport;
import com.galenframework.speclang2.pagespec.SectionFilter;
import com.galenframework.specs.Spec;
import com.galenframework.utils.GalenUtils;
import com.galenframework.validation.ValidationError;
import com.galenframework.validation.ValidationObject;
import com.galenframework.validation.ValidationResult;
import nl.hsac.fitnesse.fixture.Environment;
import nl.hsac.fitnesse.fixture.slim.SlimFixtureWithMap;
import nl.hsac.fitnesse.fixture.util.FileUtil;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Fixture to check web page layout using Galen Framework.
 * @link http://galenframework.com
 */
public class LayoutTest extends SlimFixtureWithMap {
    private static final String REPORT_OVERVIEW_SYMBOL = "GALEN_TOP_LEVEL_REPORT_INDEX";
    private static final String REPORT_SUBDIR = String.valueOf(new Date().getTime());
    private static final List<GalenTestInfo> ALL_TESTS = new LinkedList<>();

    private String reportBase = new File(filesDir, "galen-reports/" + REPORT_SUBDIR).getPath();
    private List<String> includedTags = Collections.emptyList();
    private List<String> excludedTags = Collections.emptyList();
    private String layoutCheckName;

    private LayoutReport layoutReport = new LayoutReport();
    private TestStatistic testStatistic = new TestStatistic();

    public String verifyLayoutUsing(String specFile) throws IOException {
        String specPath = getFilePathFromWikiUrl(specFile);
        GalenTestInfo test = createGalenTestInfo();

        checkLayout(specPath, test);

        return report();
    }

    protected void checkLayout(String specPath, GalenTestInfo test) throws IOException {
        String reportTitle = getReportTitle(specPath, includedTags(), excludedTags());
        SectionFilter sectionFilter = new SectionFilter(includedTags(), excludedTags());
        checkLayout(specPath, test, reportTitle, sectionFilter, new Properties(), getCurrentValues());
    }

    protected void checkLayout(String specPath, GalenTestInfo test, String reportTitle,
                               SectionFilter sectionFilter, Properties properties, Map<String, Object> jsVariables)
            throws IOException {
        TestReport report = test.getReport();
        // ensure we reset test statistic before each call
        testStatistic = new TestStatistic();
        layoutReport = Galen.checkLayout(getDriver(), specPath, sectionFilter, properties, jsVariables);

        // Adding layout report to the test report
        report.layout(layoutReport, reportTitle);
        testStatistic = report.fetchStatistic();

        ALL_TESTS.add(test);
        // re-set name for next test
        setLayoutCheckName(null);
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

    protected GalenTestInfo createGalenTestInfo() {
        String name = getGalenTestInfoName();
        return GalenTestInfo.fromString(name);
    }

    protected String getGalenTestInfoName() {
        String name = layoutCheckName();
        return name == null ?
                String.format("FitNesse%s%s", getClass().getSimpleName(), ALL_TESTS.size()) : name;
    }

    public int verifiedSpecCount() {
        return getTestStatistic().getTotal();
    }

    public int passedSpecCount() {
        return getTestStatistic().getPassed();
    }

    public int specErrorCount() {
        return getTestStatistic().getErrors();
    }

    public int specWarningCount() {
        return getTestStatistic().getWarnings();
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

    public void setLayoutCheckName(String testName) {
        this.layoutCheckName = testName;
    }

    protected String layoutCheckName() {
        return layoutCheckName;
    }

    protected String report() throws IOException {
        generateHtmlReports();
        int testCount = ALL_TESTS.size();
        GalenTestInfo last = ALL_TESTS.get(testCount - 1);
        return createLinkToGalenReport(testCount, last);
    }

    protected String createLinkToGalenReport(int testCount, GalenTestInfo last) {
        String baseName = GalenUtils.convertToFileName(last.getName());
        String fileName = String.format("%s-%s.html", testCount, baseName);
        String testPath = new File(getReportBase(), fileName).getPath();
        return String.format("<a href=\"%s\">%s</a>", getWikiUrl(testPath), fileName);
    }

    protected void generateHtmlReports() throws IOException {
        String dir = getReportBase();
        new HtmlReportBuilder().build(ALL_TESTS, dir);
        String link = createRelativeLinkToOverallReport(dir);
        getEnvironment().setSymbol(REPORT_OVERVIEW_SYMBOL, link);
    }

    protected String createRelativeLinkToOverallReport(String dir) {
        String report = new File(dir, "report.html").getPath();
        String rootDir = getEnvironment().getFitNesseRootDir();
        return FileUtil.getRelativePath(rootDir, report);
    }

    protected LayoutReport getLayoutReport() {
        return layoutReport;
    }

    protected TestStatistic getTestStatistic() {
        return testStatistic;
    }

    protected String getReportBase() {
        return reportBase;
    }

    protected WebDriver getDriver() {
        return getEnvironment().getSeleniumHelper().driver();
    }

    public static String getOverallReportLink() {
        String file = Environment.getInstance().getSymbol(REPORT_OVERVIEW_SYMBOL);
        return file == null ? null : String.format("<a href=\"%s\">Layout Test's Galen Reports</a>", file);
    }
}
