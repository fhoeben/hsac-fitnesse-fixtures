package nl.hsac.fitnesse.junit.reportmerge;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

public class ReportFinder {
    public List<TestReportHtml> findTestResultPages(File parentDir) throws IOException {
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

    protected boolean isNotIndexHtml(File file) {
        return !"index.html".equals(file.getName());
    }

    protected TestReportFactory getReportFactory(File parentDir) {
        return new TestReportFactory(parentDir);
    }
}
