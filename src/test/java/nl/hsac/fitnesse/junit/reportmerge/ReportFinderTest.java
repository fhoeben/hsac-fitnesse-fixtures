package nl.hsac.fitnesse.junit.reportmerge;

import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static nl.hsac.fitnesse.junit.reportmerge.HtmlReportIndexGeneratorTest.EXPECTED_TEST_COUNT;
import static nl.hsac.fitnesse.junit.reportmerge.HtmlReportIndexGeneratorTest.getTestReportsPath;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReportFinderTest {

    @Test
    public void testFindTestResultPages() throws Exception {
        String path = getTestReportsPath();
        List<TestReportHtml> reports = new ReportFinder().findTestResultPages(new File(path));
        List<TestReportHtml> overviews = reports.stream().filter(TestReportHtml::isOverviewPage).collect(Collectors.toList());
        assertEquals("Unexpected number of run: " + overviews, 3, overviews.size());
        assertEquals("Unexpected number of results", EXPECTED_TEST_COUNT, reports.size());

        TestReportHtml mockXmlServerTest = getActual(reports, "MockXmlServerTest");
        assertEquals(-1, mockXmlServerTest.getTime());
        assertEquals(Integer.MAX_VALUE - 2, mockXmlServerTest.getIndex());
        TestReportHtml mockXmlSuiteSetup = getReport(reports, "MockXmlServerTest", "SuiteSetUp");
        assertEquals(Integer.MAX_VALUE - 3, mockXmlSuiteSetup.getIndex());

        assertTrue(reports.stream().filter(r -> !"MockXmlServerTest".equals(r.getRunName())).noneMatch(r -> r.getTime() == -1));

        TestReportHtml fitOverview = getActual(overviews, "Fit");
        assertEquals(0, fitOverview.getTime());
        assertEquals(Integer.MAX_VALUE, fitOverview.getIndex());
        assertEquals(2400, getActual(overviews, "Http").getTime());
        assertEquals(2075, getActual(overviews, "Util").getTime());

        TestReportHtml fitSuiteSetup = getReport(reports, "Fit", "SuiteSetUp");
        assertEquals(0, fitSuiteSetup.getIndex());
        TestReportHtml fitArraysAndSymbols = getReport(reports, "Fit", "ArraysAndSymbolsComparison");
        assertEquals(1, fitArraysAndSymbols.getIndex());
        TestReportHtml fitReturn = getReport(reports, "Fit", "ReturnArrayAsSymbol");
        assertEquals(2, fitReturn.getIndex());
    }

    private TestReportHtml getReport(List<TestReportHtml> reports, String run, String test) {
        return reports.stream()
                .filter(r -> run.equals(r.getRunName()) && r.getTestName().endsWith(test))
                .findFirst()
                .get();
    }

    private TestReportHtml getActual(List<TestReportHtml> list, String runName) {
        return list.stream()
                .filter(r -> runName.equals(r.getRunName()))
                .findFirst()
                .get();
    }

}
