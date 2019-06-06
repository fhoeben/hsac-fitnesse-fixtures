package nl.hsac.fitnesse.junit.reportmerge;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Date;

/**
 * Wrapper around FitNesse test result HTML page.
 */
public class TestReportHtml implements Comparable<TestReportHtml> {
    public static final String ERROR_STATUS = "error";
    public static final String FAIL_STATUS = "fail";
    public static final String PASS_STATUS = "pass";
    public static final String IGNORE_STATUS = "ignore";
    public static final String NO_TEST_STATUS = "no-test";

    private final Path htmlFilePath;
    private final FileTime fileTime;
    private final String relativePath;
    private final boolean isOverviewPage;
    private final String status;
    private long time = -2;
    private int index = Integer.MAX_VALUE;

    public TestReportHtml(File parentDir, Path htmlFilePath, boolean isOverviewPage, String status) {
        this.htmlFilePath = htmlFilePath;
        this.isOverviewPage = isOverviewPage;
        this.status = status;
        this.relativePath = getRelativePath(parentDir);
        this.fileTime = determineTime(htmlFilePath);
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
        return splitPageName(relativePath)[1].replace(".html", "");
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

    protected FileTime determineTime(Path path) {
        FileTime result = FileTime.fromMillis(0);
        try {
            BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
            FileTime creationTime = attributes.creationTime();
            if (creationTime.equals(result)) {
                // file system doesn't support creation time and didn't give modification time automatically
                result = attributes.lastModifiedTime();
            } else {
                result = creationTime;
            }
        } catch (IOException e) {
            // ignore
        }
        return result;
    }

    protected File getHtmlFile() {
        return htmlFilePath.toFile();
    }

    protected FileTime getFileTime() {
        return fileTime;
    }

    public Date getTimestamp() {
        return new Date(fileTime.toMillis());
    }

    public String getStatus() {
        return status;
    }

    public boolean isOverviewPage() {
        return isOverviewPage;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return getRunName() + " " + (isOverviewPage()? "overview" : getTestName());
    }

    @Override
    public int compareTo(TestReportHtml other) {
        int result = getFileTime().compareTo(other.getFileTime());
        if (result == 0) {
            result = getRunName().compareTo(other.getRunName());
            if (result == 0) {
                result = getIndex() - other.getIndex();
            }
        }
        return result;

    }
}
