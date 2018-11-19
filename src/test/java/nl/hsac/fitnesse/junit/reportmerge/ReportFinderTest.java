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

        assertEquals(-1, getActual(reports, "MockXmlServerTest").getTime());
        assertTrue(reports.stream().filter(r -> !"MockXmlServerTest".equals(r.getRunName())).noneMatch(r -> r.getTime() == -1));

        assertEquals(0, getActual(overviews, "Fit").getTime());
        assertEquals(2979, getActual(overviews, "Http").getTime());
        assertEquals(2435, getActual(overviews, "Util").getTime());
    }

    private TestReportHtml getActual(List<TestReportHtml> list, String runName) {
        return list.stream().filter(r -> runName.equals(r.getRunName())).findFirst().get();
    }

}
