package nl.hsac.fitnesse.junit.reportmerge;

import nl.hsac.fitnesse.fixture.util.FileUtil;
import nl.hsac.fitnesse.junit.HsacFitNesseRunner;
import nl.hsac.fitnesse.junit.reportmerge.writer.CsvOverviewFileWriter;
import nl.hsac.fitnesse.junit.reportmerge.writer.HtmlOverviewFileWriter;
import nl.hsac.fitnesse.junit.reportmerge.writer.JsonOverviewFileWriter;
import nl.hsac.fitnesse.junit.reportmerge.writer.JsonWriter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

/**
 * Creates a (single) overview page based on a pre-existing set of FitNesse result HTML pages.
 * This is useful to generate a combined result page when multiple suites were run separately (e.g. in parallel).
 */
public class HtmlReportIndexGenerator {
    public static void main(String[] arguments) throws IOException {
        String path = HsacFitNesseRunner.FITNESSE_RESULTS_PATH;
        String target = null;
        if (arguments != null && arguments.length > 0) {
            path = arguments[0];
            if (arguments.length > 1) {
                target = arguments[1];
            }
        }
        System.out.println("Generating based on: " + path);
        String output = new HtmlReportIndexGenerator().createFrom(path, target);
        System.out.println("Generated overview: " + output);
    }

    public String createFrom(String path, String target) throws IOException {
        File parentDir = new File(path);
        if (!parentDir.exists() || !parentDir.isDirectory()) {
            throw new IllegalArgumentException(parentDir.getAbsolutePath() + " is not an existing directory");
        }
        if (target != null) {
            FileUtil.copyTree(path, target);
            parentDir = new File(target);
        }

        List<TestReportHtml> reportHtmls = findTestResultPages(parentDir);
        if (reportHtmls.isEmpty()) {
            throw new IllegalArgumentException("No results found below: " + parentDir.getAbsolutePath());
        }
        return createOverviewFiles(parentDir, reportHtmls);
    }

    protected List<TestReportHtml> findTestResultPages(File parentDir) throws IOException {
        return new ReportFinder().findTestResultPages(parentDir);
    }

    protected String createOverviewFiles(File parentDir, List<TestReportHtml> reportHtmls) throws IOException {
        Collections.sort(reportHtmls);
        createJsonOverviewFile(parentDir, reportHtmls);
        createCsvOverviewFile(parentDir, reportHtmls);
        return createHtmlOverviewFile(parentDir, reportHtmls);
    }

    protected String createJsonOverviewFile(File parentDir, List<TestReportHtml> reportHtmls) throws IOException {
        return new JsonOverviewFileWriter(parentDir, this::createJsonWriter).write(reportHtmls);
    }

    protected JsonWriter createJsonWriter(PrintWriter pw) {
        return new JsonWriter(pw);
    }

    protected String createCsvOverviewFile(File parentDir, List<TestReportHtml> reportHtmls) throws IOException {
        return new CsvOverviewFileWriter(parentDir).write(reportHtmls);
    }

    protected String createHtmlOverviewFile(File parentDir, List<TestReportHtml> htmls) throws IOException {
        return new HtmlOverviewFileWriter(parentDir, this::createJsonWriter).write(htmls);
    }
}
