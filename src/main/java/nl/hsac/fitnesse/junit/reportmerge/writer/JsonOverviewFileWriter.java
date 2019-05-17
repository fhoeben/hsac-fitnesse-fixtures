package nl.hsac.fitnesse.junit.reportmerge.writer;

import nl.hsac.fitnesse.junit.reportmerge.TestReportHtml;

import java.io.File;
import java.util.Date;
import java.util.List;

public class JsonOverviewFileWriter extends OverviewFileWriter {

    public JsonOverviewFileWriter(File parentDir) {
        super(parentDir, "test-results.json");
    }

    @Override
    protected void writeContent(List<TestReportHtml> reports) {
        boolean first = true;

        pw.write("[");
        for (TestReportHtml report : reports) {
            if (first) {
                first = false;
            } else {
                pw.write(",");
            }
            writeReport(report);
        }
        pw.write("]");
    }

    protected void writeReport(TestReportHtml report) {
        pw.write("{");
        write("timestamp", report.getTimestamp());
        pw.write(",");
        write("runName", report.getRunName());
        pw.write(",");
        write("index", report.getIndex());
        pw.write(",");
        write("testName", report.getTestName());
        pw.write(",");
        write("status", report.getStatus());
        pw.write(",");
        write("time", report.getTime());
        pw.write(",");
        write("overviewPage", report.isOverviewPage());
        pw.write(",");
        write("relativePath", report.getRelativePath());
        pw.write("}");
    }

    protected void write(String key, String value) {
        writeKeyValue(key, "\"" + value + "\"");
    }

    protected void write(String key, boolean value) {
        writeKeyValue(key, value ? "true" : "false");
    }

    protected void write(String key, int value) {
        writeKeyValue(key, Integer.toString(value));
    }

    protected void write(String key, Date value) {
        write(key, value.getTime());
    }

    protected void write(String key, Long value) {
        writeKeyValue(key, Long.toString(value));
    }

    protected void writeKeyValue(String key, String value) {
        pw.write("\"");
        pw.write(key);
        pw.write("\":");
        pw.write(value);
    }
}
