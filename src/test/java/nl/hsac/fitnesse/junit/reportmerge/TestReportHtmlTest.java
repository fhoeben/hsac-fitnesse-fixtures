package nl.hsac.fitnesse.junit.reportmerge;

import nl.hsac.fitnesse.fixture.util.ReflectionHelper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestReportHtmlTest {

    private final ReflectionHelper reflectionHelper = new ReflectionHelper();

    @Test
    public void compareBasedOnRun() {
        String reportFile = "Fit/index.html";
        TestReportHtml report = createReportHtml(reportFile);
        TestReportHtml report2 = createReportHtml(reportFile);

        assertEquals(0, report.compareTo(report2));

        reflectionHelper.setField(report2, "relativePath", "Fit2/index.html");
        assertEquals(-1, report.compareTo(report2));
        assertEquals(1, report2.compareTo(report));
    }

    @Test
    public void compareBasedOnIndex() {
        String reportFile = "Fit/index.html";
        TestReportHtml report = createReportHtml(reportFile);
        TestReportHtml report2 = createReportHtml(reportFile);
        report.setIndex(1);
        report2.setIndex(1);
        assertEquals(0, report.compareTo(report2));

        report.setIndex(1);
        report2.setIndex(2);
        assertEquals(-1, report.compareTo(report2));

        report.setIndex(2);
        report2.setIndex(1);
        assertEquals(1, report.compareTo(report2));
    }

    @Test
    public void compareBasedOnTime() throws IOException {
        File newFile = new File(HtmlReportIndexGeneratorTest.getTestReportsPath(), "Util/newReport.html");
        try {
            if (newFile.exists()) {
                assertTrue(newFile.delete());
            }
            assertTrue(newFile.createNewFile());

            TestReportHtml report = createReportHtml("Util/HsacAcceptanceTests.SlimTests.UtilityFixtures.CompareFixtureTest.html");
            TestReportHtml report2 = createReportHtml("Util/newReport.html");
            report.setIndex(1);
            report2.setIndex(1);

            assertEquals(-1, report.compareTo(report2));
            assertEquals(1, report2.compareTo(report));
        } finally {
            if (newFile.exists()) {
                assertTrue(newFile.delete());
            }
        }
    }

    private TestReportHtml createReportHtml(String reportFile) {
        File parent = new File(HtmlReportIndexGeneratorTest.getTestReportsPath());
        Path p = new File(parent, reportFile).toPath();
        return new TestReportHtml(parent, p, false, "passed");
    }
}
