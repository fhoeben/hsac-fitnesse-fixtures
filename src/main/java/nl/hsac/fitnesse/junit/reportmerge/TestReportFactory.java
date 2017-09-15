package nl.hsac.fitnesse.junit.reportmerge;

import nl.hsac.fitnesse.fixture.util.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Factory to create TestReportHtml instances.
 */
public class TestReportFactory {
    protected static final String OVERVIEW_TABLE_START = "<table><tr><td>Name</td><td>Right</td><td>Wrong</td><td>Exceptions</td></tr>";
    protected final File rootDir;

    public TestReportFactory(File rootDir) {
        this.rootDir = rootDir;
    }

    public TestReportHtml create(File reportFile) {
        String content = getFileContent(reportFile);
        boolean isOverview = content.contains(OVERVIEW_TABLE_START);
        String status = getStatus(content);
        return new TestReportHtml(rootDir, reportFile, isOverview, status);
    }

    protected String getStatus(String content) {
        String result;
        if (content.contains("class=\"error\">")) {
            result = "error";
        } else if (content.contains("class=\"fail\">")) {
            result = "fail";
        } else if (content.contains("class=\"pass\">")) {
            result = "pass";
        } else {
            result = "ignore";
        }
        return result;
    }

    protected static String getFileContent(File file) {
        try (FileInputStream s = new FileInputStream(file)) {
            return FileUtil.streamToString(s, file.getName()).trim();
        } catch (IOException e) {
            return "";
        }
    }
}
