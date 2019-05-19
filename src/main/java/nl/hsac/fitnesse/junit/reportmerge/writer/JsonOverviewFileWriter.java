package nl.hsac.fitnesse.junit.reportmerge.writer;

import nl.hsac.fitnesse.junit.reportmerge.TestReportHtml;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.function.Function;

public class JsonOverviewFileWriter extends OverviewFileWriter {
    protected Function<PrintWriter, JsonWriter> jsonWriterFunction;

    public JsonOverviewFileWriter(File parentDir, Function<PrintWriter, JsonWriter> jsonWriterFunction) {
        super(parentDir, "test-results.json");
        this.jsonWriterFunction = jsonWriterFunction;
    }

    @Override
    protected void writeContent(List<TestReportHtml> reports) {
        jsonWriterFunction.apply(pw).writeContent(reports);
    }
}
