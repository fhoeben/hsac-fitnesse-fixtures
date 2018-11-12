package nl.hsac.fitnesse.junit.reportmerge;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public abstract class OverviewFileWriter {
    private final File file;
    protected PrintWriter pw;

    public OverviewFileWriter(File parentDir, String name) {
        file = new File(parentDir, name);
    }

    public String write(List<TestReportHtml> reports) throws IOException {
        pw = new PrintWriter(file, StandardCharsets.UTF_8.name());
        try {
            writeContent(reports);
        } finally {
            if (pw != null) {
                pw.flush();
                pw.close();
            }
        }
        return file.getAbsolutePath();
    }

    protected abstract void writeContent(List<TestReportHtml> reports) throws IOException;
}
