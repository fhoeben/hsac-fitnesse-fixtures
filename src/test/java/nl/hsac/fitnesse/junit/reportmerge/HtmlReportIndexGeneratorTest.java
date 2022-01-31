package nl.hsac.fitnesse.junit.reportmerge;

import nl.hsac.fitnesse.fixture.util.FileUtil;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class HtmlReportIndexGeneratorTest {
    static final int EXPECTED_TEST_COUNT = 43;
    private HtmlReportIndexGenerator generator = new HtmlReportIndexGenerator();

    @Test
    public void testBadParent() throws IOException {
        String path = "htmlReports";
        try {
            String result = generator.createFrom(path, null);
            fail("Expected exception, got: " + result);
        } catch (IllegalArgumentException e) {
            assertTrue("Bad message: " + e.getMessage(), e.getMessage().endsWith("htmlReports is not an existing directory"));
        }
    }

    @Test
    public void testCreateFrom() throws Exception {
        String path = getTestReportsPath();

        File report = checkCreateFrom(path, null);

        assertEquals(new File(path).getAbsolutePath(), report.getParentFile().getAbsolutePath());
    }

    @Test
    public void testCreateFromWithTarget() throws Exception {
        String path = getTestReportsPath();
        File tempDir = Files.createTempDirectory("report-gen-parent").toFile();
        String targetDir = tempDir.getAbsolutePath();

        File report = checkCreateFrom(path, targetDir);

        assertEquals(targetDir, report.getParentFile().getAbsolutePath());
    }

    private File checkCreateFrom(String path, String target) throws IOException {
        String resultFile = generator.createFrom(path, target);
        assertNotNull(resultFile);

        File report = new File(resultFile);
        assertEquals("index.html", FilenameUtils.getName(resultFile));
        assertTrue(report.getAbsolutePath() + " does not exist", report.exists());

        try (FileInputStream s = new FileInputStream(report)) {
            String contents = FileUtil.streamToString(s, resultFile).trim();

            assertTrue("File does not start with expected content, but was:\n" + contents, contents.startsWith("<html><head>"));
            assertTrue("File does not end with expected content, but was:\n" + contents, contents.endsWith("</body></html>"));
            assertTrue("File does not have expected CSS link:\n" + contents, contents.contains("href='Fit/css/fitnesse.css'"));
            assertTrue("File does not have Overview Pages section:\n" + contents, contents.contains("<h2>Overview Pages</h2>"));
            String[] rows = contents.split("</tr>\\s*<tr");
            assertEquals("Unexpected number of rows: \n" + String.join("\n", rows), EXPECTED_TEST_COUNT + 1, rows.length);
        }

        File jsonReport = new File(path, "test-results.json");
        assertTrue(jsonReport.exists());
        try (FileInputStream s = new FileInputStream(jsonReport)) {
            String contents = FileUtil.streamToString(s, jsonReport.getName());
            assertTrue(contents, contents.startsWith("["));
            JSONObject jsonObject = new JSONObject("{'a': " + contents + "}");
            org.json.JSONArray array = (org.json.JSONArray) jsonObject.get("a");
            assertEquals(EXPECTED_TEST_COUNT, array.length());
        }

        File csvReport = new File(path, "test-results.csv");
        assertTrue(csvReport.exists());
        try (FileInputStream s = new FileInputStream(csvReport)) {
            String contents = FileUtil.streamToString(s, csvReport.getName());
            String[] lines = contents.split("\n");
            assertEquals(EXPECTED_TEST_COUNT + 1, lines.length);
            for (String line : lines) {
                String[] fields = line.split("\t");
                assertEquals("Unexpected number of fields in: " + line, 6, fields.length);
            }
        }
        return report;
    }

    static String getTestReportsPath() {
        String path = "src/test/resources/htmlReports";
        File pathFile = new File(path);
        assertTrue(pathFile.getAbsolutePath() + " does not exist", pathFile.exists());
        assertTrue(pathFile.getAbsolutePath() + " is not a directory", pathFile.isDirectory());
        return path;
    }

}
