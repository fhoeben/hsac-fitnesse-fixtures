package nl.hsac.fitnesse.junit.reportmerge;

import nl.hsac.fitnesse.fixture.util.FileUtil;
import nl.hsac.fitnesse.junit.HsacFitNesseRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Creates a (single) overview page based on a pre-existing set of FitNesse result HTML pages.
 * This is useful to generate a combined result page when multiple suites were run separately (e.g. in parallel).
 */
public class HtmlReportIndexGenerator {
    protected static final String OVERVIEW_TABLE_START = "<table><tr><td>Name</td><td>Right</td><td>Wrong</td><td>Exceptions</td></tr>";

    public static void main(String[] arguments) throws IOException {
        String path = HsacFitNesseRunner.FITNESSE_RESULTS_PATH;
        if (arguments != null && arguments.length > 0) {
            path = arguments[0];
        }
        System.out.println("Generating based on: " + path);
        String output = new HtmlReportIndexGenerator().createFrom(path);
        System.out.println("Generated overview: " + output);
    }

    public String createFrom(String path) throws IOException {
        File parentDir = new File(path);
        if (!parentDir.exists() || !parentDir.isDirectory()) {
            throw new IllegalArgumentException(parentDir.getAbsolutePath() + " is not an existing directory");
        }

        Map<String, String> categorized = findTestResultPages(parentDir);
        if (categorized.isEmpty()) {
            throw new IllegalArgumentException("No results found below: " + parentDir.getAbsolutePath());
        }

        return createOverviewFile(parentDir, categorized);
    }

    protected String createOverviewFile(File parentDir, Map<String, String> categorized) throws IOException {
        File newIndex = new File(parentDir, "index.html");
        createOverview(newIndex, categorized);

        return newIndex.getAbsolutePath();
    }

    protected Map<String, String> findTestResultPages(File parentDir) throws IOException {
        return Files.find(parentDir.toPath(), 2,
                    (p, name) -> p.getFileName().toString().endsWith(".html"))
                    .map(p -> p.toFile())
                    .filter(this::isNotOverviewPage)
                    .collect(toLinkedMap(f -> parentDir.toURI().relativize(f.toURI()).toString(), this::getFileCategory));
    }

    protected void createOverview(File index, Map<String, String> categorized) throws IOException {
        try (PrintWriter pw = new PrintWriter(index, "utf-8")) {
            writeHeader(pw, categorized);
            writeBody(index, pw, categorized);
            writeFooter(pw, categorized);
        }
    }

    protected void writeHeader(PrintWriter pw, Map<String, String> categorized) {
        String firstTestPage = categorized.keySet().iterator().next();
        String firstRunCssDir = splitPageName(firstTestPage)[0];
        pw.write("<html><head><meta http-equiv='Content-Type' content='text/html;charset=UTF-8'/><link rel='stylesheet' type='text/css' href='");
        pw.write(firstRunCssDir);
        pw.write("/css/fitnesse.css'/></head>");
        pw.write("<body>");
    }

    protected void writeFooter(PrintWriter pw, Map<String, String> categorized) {
        pw.write("</body></html>");
    }

    protected void writeBody(File index, PrintWriter pw, Map<String, String> categorized) {
        writeOverviewSection(index, pw, categorized);
        writeTestResultsSection(pw, categorized);
    }

    protected void writeOverviewSection(File index, PrintWriter pw, Map<String, String> categorized) {
        Map<String, String> overviewPages = filterByStatus(categorized, "overview");
        overviewPages = fillStatusForOverviews(index.getParentFile(), overviewPages);
        writeSection(pw, "Overview Pages", overviewPages);
    }

    protected void writeTestResultsSection(PrintWriter pw, Map<String, String> categorized) {
        pw.write("<div id=\"TestResults\">");
        Map<String, String> erroredTests = filterByStatus(categorized, "error");
        writeSection(pw, "Errored Tests", erroredTests);
        Map<String, String> failedTests = filterByStatus(categorized, "fail");
        writeSection(pw, "Failed Tests", failedTests);

        Map<String, String> ignoredTests = filterByStatus(categorized, "ignore");
        writeSection(pw, "Ignored Tests", ignoredTests);
        Map<String, String> passedTests = filterByStatus(categorized, "pass");
        writeSection(pw, "Passed Tests", passedTests);
        pw.write("</div>");
    }

    protected Map<String, String> fillStatusForOverviews(File parentFile, Map<String, String> overviewPages) {
        return mapValues(overviewPages, e -> {
            String content = getFileContent(new File(parentFile, e.getKey()));
            return getStatus(content);
        });
    }

    protected void writeSection(PrintWriter pw, String header, Map<String, String> statuses) {
        if (!statuses.isEmpty()) {
            pw.write("<div id=\"");
            pw.write(header.replaceAll("\\s", ""));
            pw.write("\">");
            pw.write("<h2>");
            pw.write(header);
            pw.write("</h2>");
            writeTestsTable(pw, statuses);
            pw.write("</div>");
        }
    }

    protected void writeTestsTable(PrintWriter pw, Map<String, String> statuses) {
        pw.write("<table><tr><th>Run</th><th>Name</th></tr>");
        for (Map.Entry<String, String> entry : statuses.entrySet()) {
            String testPage = entry.getKey();
            String status = entry.getValue();
            writeTestRow(pw, testPage, status);
        }
        pw.write("</table>");
    }

    protected void writeTestRow(PrintWriter pw, String testPageName, String status) {
        String[] nameParts = splitPageName(testPageName);
        String run = nameParts[0];
        String test = nameParts[1];
        pw.write("<tr class=\"");
        pw.write(status);
        pw.write("\">");
        pw.write("<td>");
        pw.write(run);
        pw.write("</td>");
        pw.write("<td><a href=\"");
        pw.write(testPageName);
        pw.write("\">");
        pw.write(test);
        pw.write("</a></td></tr>");
    }

    protected <K, V> Map<K, V> filterByStatus(Map<K, V> map, String desiredStatus) {
        return filterToLinkedMap(map, e -> desiredStatus.equals(e.getValue()));
    }

    protected String getFileCategory(File htmlFile) {
        String result;
        String content = getFileContent(htmlFile);
        if (content.contains(OVERVIEW_TABLE_START)) {
            result = "overview";
        } else {
            result = getStatus(content);
        }
        return result;
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

    protected String[] splitPageName(String testPageName) {
        return testPageName.split("/");
    }

    protected boolean isNotOverviewPage(File file) {
        return !"index.html".equals(file.getName());
    }

    protected static String getFileContent(File file) {
        try (FileInputStream s = new FileInputStream(file)) {
            return FileUtil.streamToString(s, file.getName()).trim();
        } catch (IOException e) {
            return "";
        }
    }

    protected static <K, V> Map<K, V> mapValues(Map<K, V> map, Function<Map.Entry<K, V>, V> function) {
        return map.entrySet().stream()
                .collect(toLinkedMap(Map.Entry::getKey, function));
    }

    protected static <K, V> Map<K, V> filterToLinkedMap(Map<K, V> map, Predicate<Map.Entry<K, V>> predicate) {
        return map.entrySet().stream()
                .filter(predicate)
                .collect(toLinkedMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    protected static <T, K, U>  Collector<T, ?, Map<K, U>> toLinkedMap(Function<? super T, ? extends K> keyMapper,
                                    Function<? super T, ? extends U> valueMapper) {
        return Collectors.toMap(keyMapper, valueMapper, throwingMerger(), LinkedHashMap::new);
    }

    protected static <T> BinaryOperator<T> throwingMerger() {
        return (u,v) -> { throw new IllegalStateException("Duplicate key: " + u); };
    }
}
