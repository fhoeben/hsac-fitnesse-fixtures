package nl.hsac.fitnesse.junit.reportmerge.writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.hsac.fitnesse.junit.reportmerge.TestReportHtml;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class JsonOverviewFileWriter extends OverviewFileWriter {
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonOverviewFileWriter(File parentDir) {
        super(parentDir, "test-results.json");
    }

    @Override
    protected void writeContent(List<TestReportHtml> reports) throws IOException {
        mapper.writeValue(pw, reports);
    }
}
