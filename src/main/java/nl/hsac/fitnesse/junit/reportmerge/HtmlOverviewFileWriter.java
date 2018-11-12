package nl.hsac.fitnesse.junit.reportmerge;

import java.io.File;
import java.io.PrintWriter;
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

public class HtmlOverviewFileWriter extends OverviewFileWriter {
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

    public HtmlOverviewFileWriter(File parentDir) {
        super(parentDir, "index.html");
    }

    @Override
    protected void writeContent(List<TestReportHtml> htmls) {
        writeHeader(pw, htmls);
        writeBody(pw, htmls);
        writeFooter(pw, htmls);
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

    protected ChartWriter getChartWriter(PrintWriter pw) {
        return new ChartWriter(pw);
    }

    protected static List<TestReportHtml> filterByStatus(List<TestReportHtml> htmls, String desiredStatus) {
        return filterBy(htmls, x -> desiredStatus.equals(x.getStatus()));
    }

    protected static <T> List<T> filterBy(List<T> list, Predicate<T> predicate) {
        return list.stream().filter(predicate).collect(Collectors.toList());
    }
}
