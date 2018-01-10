package nl.hsac.fitnesse.junit.reportmerge;

import nl.hsac.fitnesse.junit.HsacFitNesseRunner;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.List;
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

        return createOverviewFile(parentDir, reportHtmls);
    }

    protected String createOverviewFile(File parentDir, List<TestReportHtml> htmls) throws IOException {
        File newIndex = new File(parentDir, "index.html");
        createOverview(newIndex, htmls);

        return newIndex.getAbsolutePath();
    }

    protected List<TestReportHtml> findTestResultPages(File parentDir) throws IOException {
        TestReportFactory reportFactory = getReportFactory(parentDir);

        return Files.find(parentDir.toPath(), 2,
                    (p, name) -> p.getFileName().toString().endsWith(".html"))
                    .map(p -> p.toFile())
                    .filter(this::isNotIndexHtml)
                    .sorted()
                    .map(reportFactory::create)
                    .collect(Collectors.toList());
    }

    protected TestReportFactory getReportFactory(File parentDir) {
        return new TestReportFactory(parentDir);
    }

    protected void createOverview(File index, List<TestReportHtml> htmls) throws IOException {
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
        pw.write("/css/fitnesse.css'/></head>");
        pw.write("<body>");
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
        List<TestReportHtml> testHtmls = filterBy(htmls,
                                                    x -> !x.isOverviewPage()
                                                            && !NO_TEST_STATUS.equals(x.getStatus()));
        pw.write("<table style=\"width:100%;text-align:center;\"><tr>");
        writeGraphCell(pw, ERROR_STATUS, testHtmls);
        writeGraphCell(pw, FAIL_STATUS, testHtmls);
        writeGraphCell(pw, IGNORE_STATUS, testHtmls);
        writeGraphCell(pw, PASS_STATUS, testHtmls);
        pw.write("</tr></table>");
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

    protected void writeTestResultsSection(PrintWriter pw, List<TestReportHtml> htmls) {
        List<TestReportHtml> testHtmls = filterBy(htmls, x -> !x.isOverviewPage());
        pw.write("<div id=\"TestResults\">");
        List<TestReportHtml> erroredTests = filterByStatus(testHtmls, ERROR_STATUS);
        writeSection(pw, "Errored Tests", erroredTests);
        List<TestReportHtml> failedTests = filterByStatus(testHtmls, FAIL_STATUS);
        writeSection(pw, "Failed Tests", failedTests);

        List<TestReportHtml> ignoredTests = filterByStatus(testHtmls, IGNORE_STATUS);
        writeSection(pw, "Ignored Tests", ignoredTests);
        List<TestReportHtml> passedTests = filterByStatus(testHtmls, PASS_STATUS);
        writeSection(pw, "Passed Tests", passedTests);
        List<TestReportHtml> noTests = filterByStatus(testHtmls, NO_TEST_STATUS);
        writeSection(pw, "Pages Without Assertions", noTests);
        pw.write("</div>");
    }

    protected void writeSection(PrintWriter pw, String header, List<TestReportHtml> htmls) {
        if (!htmls.isEmpty()) {
            pw.write("<div id=\"");
            pw.write(header.replaceAll("\\s", ""));
            pw.write("\">");
            pw.write("<h2>");
            pw.write(header);
            pw.write("</h2>");
            writeTestsTable(pw, htmls);
            pw.write("</div>");
        }
    }

    protected void writeTestsTable(PrintWriter pw, List<TestReportHtml> htmls) {
        pw.write("<table><tr><th>Run</th><th>Name</th></tr>");
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
        pw.write("</a></td></tr>");
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
}
