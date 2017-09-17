package nl.hsac.fitnesse.junit.reportmerge;

import nl.hsac.fitnesse.fixture.util.FileUtil;
import org.apache.commons.io.FilenameUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.*;

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
        String path = "src/test/resources/htmlReports";
        File pathFile = new File(path);
        assertTrue(pathFile.getAbsolutePath() + " does not exist", pathFile.exists());
        assertTrue(pathFile.getAbsolutePath() + " is not a directory", pathFile.isDirectory());

        String resultFile = generator.createFrom(path);
        assertNotNull(resultFile);

        File report = new File(resultFile);
        assertEquals("index.html", FilenameUtils.getName(resultFile));
        assertEquals(pathFile.getAbsolutePath(), report.getParentFile().getAbsolutePath());
        assertTrue(report.getAbsolutePath() + " does not exist", report.exists());

        try (FileInputStream s = new FileInputStream(report)) {
            String contents = FileUtil.streamToString(s, resultFile).trim();

            assertTrue("File does not start with expected content, but was:\n" + contents, contents.startsWith("<html><head>"));
            assertTrue("File does not end with expected content, but was:\n" + contents, contents.endsWith("</body></html>"));
            assertTrue("File does not have expected CSS link:\n" + contents, contents.contains("href='Fit/css/fitnesse.css'"));
            String[] rows = contents.split("</tr>\\s*<tr");
            assertEquals("Unexpected number of rows: \n" + String.join("\n", rows), 29, rows.length);
        }
    }

}
