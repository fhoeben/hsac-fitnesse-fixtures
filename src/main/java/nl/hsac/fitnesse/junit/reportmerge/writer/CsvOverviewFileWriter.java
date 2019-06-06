package nl.hsac.fitnesse.junit.reportmerge.writer;

import nl.hsac.fitnesse.junit.reportmerge.TestReportHtml;

import java.io.File;
import java.util.List;

public class CsvOverviewFileWriter extends OverviewFileWriter {
    private String fieldSeparator = "\t";

    public CsvOverviewFileWriter(File parentDir) {
        super(parentDir, "test-results.csv");
    }

    @Override
    protected void writeContent(List<TestReportHtml> reports) {
        writeHeader();
        writeLineEnd();
        for (TestReportHtml report : reports) {
            writeRow(report);
            writeLineEnd();
        }
    }

    protected void writeHeader() {
        pw.write("Run name");
        appendField("Test name");
        appendField("Is overview");
        appendField("Status");
        appendField("Runtime (in milliseconds)");
        appendField("Relative Path");
    }

    protected void writeRow(TestReportHtml report) {
        pw.write(report.getRunName());
        appendField(report.getTestName());
        appendField(Boolean.toString(report.isOverviewPage()));
        appendField(report.getStatus());
        appendField(report.getTime() < 0 ? "unknown" : Long.toString(report.getTime()));
        appendField(report.getRelativePath());
    }

    protected void appendField(String value) {
        pw.write(getFieldSeparator());
        pw.write(value);
    }

    protected void writeLineEnd() {
        pw.write("\n");
    }

    public String getFieldSeparator() {
        return fieldSeparator;
    }

    public void setFieldSeparator(String fieldSeparator) {
        this.fieldSeparator = fieldSeparator;
    }
}
