package nl.hsac.fitnesse.junit.reportmerge;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReportFinder {
    public List<TestReportHtml> findTestResultPages(File parentDir) throws IOException {
        TestReportFactory reportFactory = getReportFactory(parentDir);

        try (Stream<Path> htmlStream = Files.find(parentDir.toPath(), 2,
                (p, attributes) -> p.getFileName().toString().endsWith(".html"))) {
            List<TestReportHtml> reportHtmls = htmlStream
                    .filter(this::isNotIndexHtml)
                    .map(reportFactory::create)
                    .collect(Collectors.toList());

            postProcessReports(reportFactory, reportHtmls);
            return reportHtmls;
        }
    }

    protected boolean isNotIndexHtml(Path file) {
        return !"index.html".equals(file.getFileName().toString());
    }

    protected void postProcessReports(TestReportFactory reportFactory, List<TestReportHtml> reportHtmls) {
        for (TestReportHtml html : reportHtmls) {
            String runName = html.getRunName();
            long time;
            int index;
            if (html.isOverviewPage()) {
                index = reportFactory.getIndex(runName);
                time = reportFactory.getTime(runName);
            } else {
                String testName = html.getTestName();
                index = reportFactory.getIndex(runName, testName);
                time = reportFactory.getTime(runName, testName);
            }
            html.setTime(time);
            html.setIndex(index);
        }
    }

    protected TestReportFactory getReportFactory(File parentDir) {
        return new TestReportFactory(parentDir);
    }
}
