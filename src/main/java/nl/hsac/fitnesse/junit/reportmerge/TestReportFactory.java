package nl.hsac.fitnesse.junit.reportmerge;

import nl.hsac.fitnesse.fixture.util.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static nl.hsac.fitnesse.junit.reportmerge.TestReportHtml.ERROR_STATUS;
import static nl.hsac.fitnesse.junit.reportmerge.TestReportHtml.FAIL_STATUS;
import static nl.hsac.fitnesse.junit.reportmerge.TestReportHtml.IGNORE_STATUS;
import static nl.hsac.fitnesse.junit.reportmerge.TestReportHtml.NO_TEST_STATUS;
import static nl.hsac.fitnesse.junit.reportmerge.TestReportHtml.PASS_STATUS;

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
            result = ERROR_STATUS;
        } else if (content.contains("class=\"fail\">")) {
            result = FAIL_STATUS;
        } else if (content.contains("class=\"pass\">")) {
            result = PASS_STATUS;
        } else if (content.contains("class=\"ignore\">")) {
            result = IGNORE_STATUS;
        } else {
            result = NO_TEST_STATUS;
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
