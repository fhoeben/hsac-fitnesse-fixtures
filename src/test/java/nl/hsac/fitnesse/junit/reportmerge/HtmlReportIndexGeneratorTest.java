package nl.hsac.fitnesse.junit.reportmerge;

import nl.hsac.fitnesse.fixture.util.FileUtil;
import org.apache.commons.io.FilenameUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class HtmlReportIndexGeneratorTest {
    private HtmlReportIndexGenerator generator = new HtmlReportIndexGenerator();

    @Test
    public void testBadParent() throws IOException {
        String path = "htmlReports";
        try {
            String result = generator.createFrom(path);
            fail("Expected exception, got: " + result);
        } catch (IllegalArgumentException e) {
            assertTrue("Bad message: " + e.getMessage(), e.getMessage().endsWith("htmlReports is not an existing directory"));
        }
    }

    @Test
    public void testCreateFrom() throws Exception {
        String path = getTestReportsPath();

        String resultFile = generator.createFrom(path);
        assertNotNull(resultFile);

        File report = new File(resultFile);
        assertEquals("index.html", FilenameUtils.getName(resultFile));
        assertEquals(new File(path).getAbsolutePath(), report.getParentFile().getAbsolutePath());
        assertTrue(report.getAbsolutePath() + " does not exist", report.exists());

        try (FileInputStream s = new FileInputStream(report)) {
            String contents = FileUtil.streamToString(s, resultFile).trim();

            assertTrue("File does not start with expected content, but was:\n" + contents, contents.startsWith("<html><head>"));
            assertTrue("File does not end with expected content, but was:\n" + contents, contents.endsWith("</body></html>"));
            assertTrue("File does not have expected CSS link:\n" + contents, contents.contains("href='Fit/css/fitnesse.css'"));
            assertTrue("File does not have Overview Pages section:\n" + contents, contents.contains("<h2>Overview Pages</h2>"));
            String[] rows = contents.split("</tr>\\s*<tr");
            assertEquals("Unexpected number of rows: \n" + String.join("\n", rows), 42, rows.length);
        }
    }

    @Test
    public void testFindTestResultPages() throws Exception {
        String path = getTestReportsPath();
        List<TestReportHtml> reports = generator.findTestResultPages(new File(path));
        List<TestReportHtml> overviews = reports.stream().filter(TestReportHtml::isOverviewPage).collect(Collectors.toList());
        assertEquals("Unexpected number of run: " + overviews, 3, overviews.size());
        assertEquals("Unexpected number of results", 41, reports.size());

        assertEquals(-1, getActual(reports, "MockXmlServerTest").getTime());
        assertTrue(reports.stream().filter(r -> !"MockXmlServerTest".equals(r.getRunName())).noneMatch(r -> r.getTime() == -1));

        assertEquals(0, getActual(overviews, "Fit").getTime());
        assertEquals(2979, getActual(overviews, "Http").getTime());
        assertEquals(2435, getActual(overviews, "Util").getTime());
    }

    private TestReportHtml getActual(List<TestReportHtml> list, String runName) {
        return list.stream().filter(r -> runName.equals(r.getRunName())).findFirst().get();
    }

    private String getTestReportsPath() {
        String path = "src/test/resources/htmlReports";
        File pathFile = new File(path);
        assertTrue(pathFile.getAbsolutePath() + " does not exist", pathFile.exists());
        assertTrue(pathFile.getAbsolutePath() + " is not a directory", pathFile.isDirectory());
        return path;
    }

}
