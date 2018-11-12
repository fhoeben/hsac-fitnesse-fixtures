package nl.hsac.fitnesse.junit.reportmerge;

import java.io.File;
import java.util.List;

public class CsvOverviewFileWriter extends OverviewFileWriter {
    private String fieldSeparator = "\t";

    public CsvOverviewFileWriter(File parentDir) {
        super(parentDir, "test-results.csv");
    }

    @Override
    protected void writeContent(List<TestReportHtml> reports) {
        pw.write("Run name");
        pw.write(fieldSeparator);
        pw.write("Test name");
        pw.write(fieldSeparator);
        pw.write("Is overview");
        pw.write(fieldSeparator);
        pw.write("Status");
        pw.write(fieldSeparator);
        pw.write("Runtime (in milliseconds)");
        pw.write(fieldSeparator);
        pw.write("Relative Path");
        pw.write("\n");
        for (TestReportHtml report : reports) {
            pw.write(report.getRunName());
            pw.write(fieldSeparator);
            pw.write(report.getTestName());
            pw.write(fieldSeparator);
            pw.write(Boolean.toString(report.isOverviewPage()));
            pw.write(fieldSeparator);
            pw.write(report.getStatus());
            pw.write(fieldSeparator);
            pw.write(report.getTime() < 0 ? "unknown" : Long.toString(report.getTime()));
            pw.write(fieldSeparator);
            pw.write(report.getRelativePath());
            pw.write("\n");
        }
    }

    public String getFieldSeparator() {
        return fieldSeparator;
    }

    public void setFieldSeparator(String fieldSeparator) {
        this.fieldSeparator = fieldSeparator;
    }
}
