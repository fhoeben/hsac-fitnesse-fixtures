package nl.hsac.fitnesse.junit.reportmerge;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.hsac.fitnesse.junit.HsacFitNesseRunner;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static nl.hsac.fitnesse.junit.reportmerge.TestReportHtml.ERROR_STATUS;
import static nl.hsac.fitnesse.junit.reportmerge.TestReportHtml.FAIL_STATUS;
import static nl.hsac.fitnesse.junit.reportmerge.TestReportHtml.IGNORE_STATUS;
import static nl.hsac.fitnesse.junit.reportmerge.TestReportHtml.NO_TEST_STATUS;
import static nl.hsac.fitnesse.junit.reportmerge.TestReportHtml.PASS_STATUS;

/**
 * Creates a (single) overview page based on a pre-existing set of FitNesse result HTML pages.
 * This is useful to generate a combined result page when multiple suites were run separately (e.g. in parallel).
 */
public class HtmlReportIndexGenerator {
    private static final String TESTCOUNT_CHART_ID = "countPie";
    private static final String RUNTIME_CHART_ID = "runtimePie";
    private static final String STATUS_CHART_ID = "statusPie";
    private static final String TIME_PER_TEST_CHART_ID = "timePerTestChart";
    private static final String COPY_TO_CLIPBOARD_JS =
            "function elementContentsToClipboard(el) {" +
                "var selected = document.getSelection().rangeCount > 0? document.getSelection().getRangeAt(0):false;" +
                "var body = document.body, range, sel;" +
                "if (document.createRange && window.getSelection) {" +
                    "range = document.createRange();" +
                    "sel = window.getSelection();" +
                    "sel.removeAllRanges();" +
                    "try {" +
                        "range.selectNodeContents(el);" +
                        "sel.addRange(range);" +
                    "} catch (e) {" +
                        "range.selectNode(el);" +
                        "sel.addRange(range);" +
                    "}" +
                    "document.execCommand('copy');" +
                "} else if (body.createTextRange) {" +
                    "range = body.createTextRange();" +
                    "range.moveToElementText(el);" +
                    "range.select();" +
                    "range.execCommand('copy');" +
                "}" +
                "document.getSelection().removeAllRanges();" +
                "if (selected) document.getSelection().addRange(selected);" +
            "}";

    private final NumberFormat nf = NumberFormat.getIntegerInstance();

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

        List<TestReportHtml> reportHtmls = findTestResultPages(parentDir);
        if (reportHtmls.isEmpty()) {
            throw new IllegalArgumentException("No results found below: " + parentDir.getAbsolutePath());
        }
        return createOverviewFiles(parentDir, reportHtmls);
    }

    protected List<TestReportHtml> findTestResultPages(File parentDir) throws IOException {
        TestReportFactory reportFactory = getReportFactory(parentDir);

        List<TestReportHtml> reportHtmls = Files.find(parentDir.toPath(), 2,
                (p, name) -> p.getFileName().toString().endsWith(".html"))
                .map(p -> p.toFile())
                .filter(this::isNotIndexHtml)
                .sorted()
                .map(reportFactory::create)
                .collect(Collectors.toList());
        for (TestReportHtml html : reportHtmls) {
            String runName = html.getRunName();
            long time = html.isOverviewPage() ?
                    reportFactory.getTime(runName)
                    : reportFactory.getTime(runName, html.getTestName());
            html.setTime(time);
        }
        return reportHtmls;
    }

    protected TestReportFactory getReportFactory(File parentDir) {
        return new TestReportFactory(parentDir);
    }

    protected String createOverviewFiles(File parentDir, List<TestReportHtml> reportHtmls) throws IOException {
        createJsonOverviewFile(parentDir, reportHtmls);
        createCsvOverviewFile(parentDir, reportHtmls);
        return createHtmlOverviewFile(parentDir, reportHtmls);
    }

    protected String createJsonOverviewFile(File parentDir, List<TestReportHtml> reportHtmls) throws IOException {
        File newFile = new File(parentDir, "test-results.json");
        createJsonOverview(newFile, reportHtmls);
        return newFile.getAbsolutePath();
    }

    protected String createCsvOverviewFile(File parentDir, List<TestReportHtml> reportHtmls) throws IOException {
        File newFile = new File(parentDir, "test-results.csv");
        createCsvOverview(newFile, reportHtmls);
        return newFile.getAbsolutePath();
    }

    protected String createHtmlOverviewFile(File parentDir, List<TestReportHtml> htmls) throws IOException {
        File newIndex = new File(parentDir, "index.html");
        createHtmlOverview(newIndex, htmls);

        return newIndex.getAbsolutePath();
    }

    protected void createJsonOverview(File newFile, List<TestReportHtml> reportHtmls) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try (PrintWriter pw = new PrintWriter(newFile, "utf-8")) {
            mapper.writeValue(pw, reportHtmls);
        }
    }

    protected void createCsvOverview(File newFile, List<TestReportHtml> reportHtmls) throws IOException {
        try (PrintWriter pw = new PrintWriter(newFile, "utf-8")) {
            pw.write("Run name");
            pw.write("\t");
            pw.write("Test name");
            pw.write("\t");
            pw.write("Status");
            pw.write("\t");
            pw.write("Runtime (in milliseconds)");
            pw.write("\t");
            pw.write("Relative Path");
            pw.write("\n");
            for (TestReportHtml report : reportHtmls) {
                pw.write(report.getRunName());
                pw.write("\t");
                pw.write(report.getTestName());
                pw.write("\t");
                pw.write(report.getStatus());
                pw.write("\t");
                pw.write(report.getTime() < 0 ? "unknown" : Long.toString(report.getTime()));
                pw.write("\t");
                pw.write(report.getRelativePath());
                pw.write("\n");
            }
        }
    }

    protected void createHtmlOverview(File index, List<TestReportHtml> htmls) throws IOException {
        try (PrintWriter pw = new PrintWriter(index, "utf-8")) {
            writeHeader(pw, htmls);
            writeBody(pw, htmls);
            writeFooter(pw, htmls);
        }
    }

    protected void writeHeader(PrintWriter pw, List<TestReportHtml> htmls) {
        TestReportHtml firstTestPage = htmls.get(0);
        String firstRunCssDir = firstTestPage.getDirectory();
        pw.write("<html><head><meta http-equiv='Content-Type' content='text/html;charset=UTF-8'/><link rel='stylesheet' type='text/css' href='");
        pw.write(firstRunCssDir);
        pw.write("/css/fitnesse.css'/>");
        pw.write("<script type='text/javascript'>");
        pw.write(COPY_TO_CLIPBOARD_JS);
        pw.write("</script>");
        writeExtraHeaderContent(pw, htmls);
        pw.write("</head><body>");
    }

    protected void writeExtraHeaderContent(PrintWriter pw, List<TestReportHtml> htmls) {
        getChartWriter(pw).writeLoadScriptTag();
    }

    protected void writeFooter(PrintWriter pw, List<TestReportHtml> htmls) {
        pw.write("</body></html>");
    }

    protected void writeBody(PrintWriter pw, List<TestReportHtml> htmls) {
        writeOverviewSection(pw, htmls);
        writeTestResultsSection(pw, htmls);
    }

    protected void writeOverviewSection(PrintWriter pw, List<TestReportHtml> htmls) {
        writeOverviewGraph(pw, htmls);

        List<TestReportHtml> overviewPages = filterBy(htmls, TestReportHtml::isOverviewPage);
        writeSection(pw, "Overview Pages", overviewPages);
    }

    protected void writeOverviewGraph(PrintWriter pw, List<TestReportHtml> htmls) {
        pw.write("<div style='width:100%;'>");

        List<TestReportHtml> testHtmls = filterBy(htmls,
                x -> !x.isOverviewPage()
                        && !NO_TEST_STATUS.equals(x.getStatus()));
        pw.write("<table style='width:100%;text-align:center;' id='simpleStatusGraph'><tr>");
        writeGraphCell(pw, ERROR_STATUS, testHtmls);
        writeGraphCell(pw, FAIL_STATUS, testHtmls);
        writeGraphCell(pw, IGNORE_STATUS, testHtmls);
        writeGraphCell(pw, PASS_STATUS, testHtmls);
        pw.write("</tr></table>");

        writePieChartsElement(pw, htmls);
        pw.write("</div>");
    }

    protected void writeGraphCell(PrintWriter pw, String status, List<TestReportHtml> testHtmls) {
        int totalCount = testHtmls.size();
        int count = filterByStatus(testHtmls, status).size();
        if (count > 0) {
            int pct = (count * 100) / totalCount;
            String cell = String.format("<td class=\"%s\" style=\"width:%s%%;\">%s</td>", status, pct, count);
            pw.write(cell);
        }
    }

    protected void writePieChartsElement(PrintWriter pw, List<TestReportHtml> htmls) {
        ChartWriter pieChartWriter = getChartWriter(pw);
        pw.write("<div style='display:flex;flex-wrap:wrap;justify-content:center;'>");
        writePieChartElements(pw, htmls);
        pieChartWriter.writeChartGenerators(
                htmls,
                this::writePieChartGeneratorBody,
                "document.getElementById('simpleStatusGraph').outerHTML=''");
        pw.write("</div>");
    }

    protected void writePieChartGeneratorBody(ChartWriter writer, List<TestReportHtml> htmls) {
        List<TestReportHtml> nonOverviews = filterBy(htmls, r -> !r.isOverviewPage());
        writeStatusPieChartGenerator(writer, nonOverviews);
        writer.writePieChartGenerator("Tests / Run", TESTCOUNT_CHART_ID, nonOverviews,
                r -> r.getRunName(), Collectors.counting());
        writer.writePieChartGenerator("Time / Run", RUNTIME_CHART_ID, nonOverviews,
                r -> r.getRunName(), Collectors.summingLong(r -> r.getTime() < 0 ? 0 : r.getTime()));

        writer.writeBarChartGenerator("ms / Test", TIME_PER_TEST_CHART_ID,
                ",hAxis:{textPosition:'none'}",
                "'Test','Runtime (ms)'", r -> r.getTestName(), r -> r.getTime(), r -> r.getRelativePath(), nonOverviews);
    }

    protected void writeStatusPieChartGenerator(ChartWriter writer, List<TestReportHtml> htmls) {
        Map<String, Long> displayedStatus = getStatusMap(htmls);
        writer.writePieChartGenerator("Status", STATUS_CHART_ID,
                ",slices:[{color:'#ffffaa'},{color:'#FF6666'},{color:'orange'},{color:'#28B463'},{color:'lightgray'}]",
                r -> r.getKey(), r -> r.getValue(), displayedStatus.entrySet());
    }

    protected void writePieChartElements(PrintWriter pw, List<TestReportHtml> htmls) {
        pw.write("<div id='");
        pw.write(STATUS_CHART_ID);
        pw.write("'></div>");
        pw.write("<div id='");
        pw.write(TESTCOUNT_CHART_ID);
        pw.write("'></div>");
        pw.write("<div id='");
        pw.write(RUNTIME_CHART_ID);
        pw.write("'></div>");
    }

    protected void writeTestResultsSection(PrintWriter pw, List<TestReportHtml> htmls) {
        List<TestReportHtml> testHtmls = filterBy(htmls, x -> !x.isOverviewPage());
        pw.write("<div id='TestResults' style='width:100%;'>");
        List<TestReportHtml> erroredTests = filterByStatus(testHtmls, ERROR_STATUS);
        writeSection(pw, "Errored Tests", erroredTests);
        List<TestReportHtml> failedTests = filterByStatus(testHtmls, FAIL_STATUS);
        writeSection(pw, "Failed Tests", failedTests);

        List<TestReportHtml> ignoredTests = filterByStatus(testHtmls, IGNORE_STATUS);
        writeSection(pw, "Ignored Tests", ignoredTests);

        pw.write("<div id='");
        pw.write(TIME_PER_TEST_CHART_ID);
        pw.write("' style='height: 300px;'></div>");

        List<TestReportHtml> passedTests = filterByStatus(testHtmls, PASS_STATUS);
        writeSection(pw, "Passed Tests", passedTests);
        List<TestReportHtml> noTests = filterByStatus(testHtmls, NO_TEST_STATUS);
        writeSection(pw, "Pages Without Assertions", noTests);
        pw.write("</div>");
    }

    protected void writeSection(PrintWriter pw, String header, List<TestReportHtml> htmls) {
        if (!htmls.isEmpty()) {
            String id = header.replaceAll("\\s", "");
            pw.write("<div id=\"");
            pw.write(id);
            pw.write("\">");
            pw.write("<h2>");
            pw.write(header);
            pw.write("</h2>");
            String tableId = id + "Table";
            pw.write("<input type='button' value='to clipboard' ");
            pw.write("onclick=\"elementContentsToClipboard(document.getElementById('");
            pw.write(tableId);
            pw.write("'));\">");
            writeTestsTable(pw, tableId, htmls);
            pw.write("</div>");
        }
    }

    protected void writeTestsTable(PrintWriter pw, String id, List<TestReportHtml> htmls) {
        pw.write("<table id='");
        pw.write(id);
        pw.write("'><tr><th>Run</th><th>Name</th><th>Runtime (in milliseconds)</th></tr>");
        // slowest times at top
        htmls.sort((o1, o2) -> Long.compare(o2.getTime(), o1.getTime()));
        for (TestReportHtml test : htmls) {
            writeTestRow(pw, test);
        }
        pw.write("</table>");
    }

    protected void writeTestRow(PrintWriter pw, TestReportHtml html) {
        String testPageName = html.getRelativePath();
        String status = html.getStatus();
        String run = html.getRunName();
        String testName = html.getTestName();
        long time = html.getTime();
        pw.write("<tr class=\"");
        pw.write(status);
        pw.write("\">");
        pw.write("<td>");
        pw.write(run);
        pw.write("</td>");
        pw.write("<td><a href=\"");
        pw.write(testPageName);
        pw.write("\">");
        pw.write(testName);
        pw.write("</a></td><td>");
        pw.write(time < 0 ? "unknown" : nf.format(time));
        pw.write("</td></tr>");
    }

    protected Map<String, Long> getStatusMap(List<TestReportHtml> htmls) {
        Map<String, Long> statuses = htmls.stream()
                .collect(Collectors.groupingBy(TestReportHtml::getStatus, Collectors.counting()));

        Map<String, Long> displayedStatus = new LinkedHashMap<>();
        addStatusEntry(ERROR_STATUS, statuses, displayedStatus);
        addStatusEntry(FAIL_STATUS, statuses, displayedStatus);
        addStatusEntry(IGNORE_STATUS, statuses, displayedStatus);
        addStatusEntry(PASS_STATUS, statuses, displayedStatus);
        addStatusEntry(NO_TEST_STATUS, statuses, displayedStatus);
        return displayedStatus;
    }

    protected void addStatusEntry(String status, Map<String, Long> statuses, Map<String, Long> displayedStatus) {
        displayedStatus.put(status, statuses.getOrDefault(status, 0L));
    }

    protected static List<TestReportHtml> filterByStatus(List<TestReportHtml> htmls, String desiredStatus) {
        return filterBy(htmls, x -> desiredStatus.equals(x.getStatus()));
    }

    protected static <T> List<T> filterBy(List<T> list, Predicate<T> predicate) {
        return list.stream().filter(predicate).collect(Collectors.toList());
    }

    protected boolean isNotIndexHtml(File file) {
        return !"index.html".equals(file.getName());
    }

    protected ChartWriter getChartWriter(PrintWriter pw) {
        return new ChartWriter(pw);
    }
}
