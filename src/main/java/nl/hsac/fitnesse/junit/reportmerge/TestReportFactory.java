package nl.hsac.fitnesse.junit.reportmerge;

import nl.hsac.fitnesse.fixture.util.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.emptyMap;
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
    protected final Map<String, Map<String, Integer>> testIndexes = new HashMap<>();

    public TestReportFactory(File rootDir) {
        this.rootDir = rootDir;
    }

    public TestReportHtml create(Path reportPath) {
        File reportFile = reportPath.toFile();
        String content = getFileContent(reportFile);
        boolean isOverview = content.contains(OVERVIEW_TABLE_START);
        String status = getStatus(content);
        TestReportHtml reportHtml = new TestReportHtml(rootDir, reportPath, isOverview, status);
        if (reportHtml.isOverviewPage()) {
            Map<String, Long> runTimes = getRunTimes(reportHtml, content);
            testTimes.put(reportHtml.getRunName(), runTimes);

            computeRunIndexes(reportHtml.getRunName(), runTimes);
        }
        return reportHtml;
    }

    public long getTime(String run) {
        return getTestTimes(run).values().stream().reduce(0L, Long::sum);
    }

    public long getTime(String run, String page) {
        Long time = getTestTimes(run).get(page);
        if (time == null) {
            time = -1L;
        }
        return time;
    }

    protected Map<String, Long> getTestTimes(String run) {
        return testTimes.computeIfAbsent(run, x -> emptyMap());
    }

    protected void computeRunIndexes(String run, Map<String, Long> runTimes) {
        int index = 0;
        Map<String, Integer> runIndexes = getTestIndexes(run);
        for (String page : runTimes.keySet()) {
            runIndexes.put(page, index++);
        }
    }

    public int getIndex(String run) {
        return Integer.MAX_VALUE;
    }

    public int getIndex(String run, String page) {
        return getTestIndexes(run)
                .computeIfAbsent(page, p -> {
                    // absent means: single test report (i.e. no overview page present)
                    int offset = 2;
                    if (page.endsWith(".SuiteSetUp")) {
                        // single test -> setup before actual test
                        offset = 3;
                    } else if (page.endsWith(".SuiteTearDown")) {
                        // single test -> teardown after actual test
                        offset = 1;
                    }
                    return getIndex(run) - offset;
                });
    }

    protected Map<String, Integer> getTestIndexes(String run) {
        return testIndexes.computeIfAbsent(run, x -> new HashMap<>());
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
        Map<String, Long> result = new LinkedHashMap<>();
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
