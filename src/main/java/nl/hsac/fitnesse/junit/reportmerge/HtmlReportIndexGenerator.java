package nl.hsac.fitnesse.junit.reportmerge;

import nl.hsac.fitnesse.junit.HsacFitNesseRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

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
        return new JsonOverviewFileWriter(parentDir).write(reportHtmls);
    }

    protected String createCsvOverviewFile(File parentDir, List<TestReportHtml> reportHtmls) throws IOException {
        return new CsvOverviewFileWriter(parentDir).write(reportHtmls);
    }

    protected String createHtmlOverviewFile(File parentDir, List<TestReportHtml> htmls) throws IOException {
        return new HtmlOverviewFileWriter(parentDir).write(htmls);
    }

    protected boolean isNotIndexHtml(File file) {
        return !"index.html".equals(file.getName());
    }
}
