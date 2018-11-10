package nl.hsac.fitnesse.junit.reportmerge;

import nl.hsac.fitnesse.fixture.util.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static nl.hsac.fitnesse.junit.reportmerge.TestReportHtml.ERROR_STATUS;
import static nl.hsac.fitnesse.junit.reportmerge.TestReportHtml.FAIL_STATUS;
import static nl.hsac.fitnesse.junit.reportmerge.TestReportHtml.IGNORE_STATUS;
import static nl.hsac.fitnesse.junit.reportmerge.TestReportHtml.NO_TEST_STATUS;
import static nl.hsac.fitnesse.junit.reportmerge.TestReportHtml.PASS_STATUS;

/**
 * Factory to create TestReportHtml instances.
 */
public class TestReportFactory {
    protected static final String OVERVIEW_TABLE_START = "<table><tr><td>Name</td><td>Right</td><td>Wrong</td><td>Exceptions</td><td>Runtime (in milliseconds)</td></tr>";
    protected static final Pattern TIME_PATTERN = Pattern.compile("</tr><tr class=.*?><td><a .*?>(.+?)</a></td><td>.+?</td><td>.+?</td><td>.+?</td><td>(\\d+?)</td>");
    protected final File rootDir;
    protected final Map<String, Map<String, Long>> testTimes = new HashMap<>();

    public TestReportFactory(File rootDir) {
        this.rootDir = rootDir;
    }

    public TestReportHtml create(File reportFile) {
        String content = getFileContent(reportFile);
        boolean isOverview = content.contains(OVERVIEW_TABLE_START);
        String status = getStatus(content);
        TestReportHtml reportHtml = new TestReportHtml(rootDir, reportFile, isOverview, status);
        if (reportHtml.isOverviewPage()) {
            Map<String, Long> runTimes = getRunTimes(reportHtml, content);
            testTimes.put(reportHtml.getRunName(), runTimes);
        }
        return reportHtml;
    }

    public long getTime(String run) {
        return getTestTimes(run).values().stream().reduce(0L, (t, a) -> t + a);
    }

    public long getTime(String run, String page) {
        Long time = getTestTimes(run).get(page);
        if (time == null) {
            time = -1L;
        }
        return time;
    }

    protected Map<String, Long> getTestTimes(String run) {
        Map<String, Long> times = testTimes.get(run);
        if (times == null) {
            times = Collections.emptyMap();
        }
        return times;
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

    protected Map<String, Long> getRunTimes(TestReportHtml reportHtml, String content) {
        HashMap<String, Long> result = new HashMap<>();
        Matcher m = TIME_PATTERN.matcher(content);
        while (m.find()) {
            String name = m.group(1);
            String time = m.group(2);
            Long msTime = Long.valueOf(time);
            result.put(name, msTime);
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
