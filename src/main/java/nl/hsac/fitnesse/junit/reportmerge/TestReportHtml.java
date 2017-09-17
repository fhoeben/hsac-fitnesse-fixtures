package nl.hsac.fitnesse.junit.reportmerge;

import java.io.File;

/**
 * Wrapper around FitNesse test result HTML page.
 */
public class TestReportHtml {
    public static final String ERROR_STATUS = "error";
    public static final String FAIL_STATUS = "fail";
    public static final String PASS_STATUS = "pass";
    public static final String IGNORE_STATUS = "ignore";
    public static final String NO_TEST_STATUS = "no-test";

    private final File htmlFile;
    private final String relativePath;
    private final boolean isOverviewPage;
    private final String status;

    protected TestReportHtml(File parentDir, File htmlFile, boolean isOverviewPage, String status) {
        this.htmlFile = htmlFile;
        this.isOverviewPage = isOverviewPage;
        this.status = status;
        this.relativePath = getRelativePath(parentDir);
    }

    public String getRelativePath() {
        return relativePath;
    }

    public String getRunName() {
        return getDirectory();
    }

    public String getDirectory() {
        return splitPageName(relativePath)[0];
    }

    public String getTestName() {
        return splitPageName(relativePath)[1];
    }

    protected String[] splitPageName(String testPageName) {
        String[] split = testPageName.split("/");
        if (split.length == 1) {
            split = new String[2];
            split[0] = "";
            split[1] = testPageName;
        }
        return split;
    }

    protected String getRelativePath(File parentDir) {
        return parentDir.toURI().relativize(getHtmlFile().toURI()).toString();
    }

    public File getHtmlFile() {
        return htmlFile;
    }

    public String getStatus() {
        return status;
    }

    public boolean isOverviewPage() {
        return isOverviewPage;
    }
}
