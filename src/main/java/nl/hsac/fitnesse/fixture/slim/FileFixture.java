package nl.hsac.fitnesse.fixture.slim;

import nl.hsac.fitnesse.fixture.util.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Utility fixture to work with files.
 */
public class FileFixture extends SlimFixtureWithMap {
    private String directory;

    public void setDirectory(String directory) {
        if (!directory.endsWith(File.separator)) {
            directory += File.separator;
        }
        this.directory = directory;
    }

    public String createContaining(String filename, String content) {
        String fullName = getFullName(filename);
        ensureParentExists(fullName);
        File f = FileUtil.writeFile(fullName, content);
        return linkToFile(f);
    }

    public String contentOf(String filename) throws IOException {
        String fullName = getFullName(filename);
        try {
            FileInputStream s = new FileInputStream(fullName);
            return FileUtil.streamToString(s, fullName);
        } catch (FileNotFoundException e) {
            throw new SlimFixtureException(false, "Unable to find: " + fullName);
        }
    }

    public String createUsing(String filename, String templateName) {
        String content = getEnvironment().processTemplate(templateName, getCurrentValues());
        return createContaining(filename, content);
    }

    public String copyTo(String sourceName, String targetName) throws IOException {
        String fullSource = getFullName(sourceName);
        File srcFile = new File(fullSource);
        if (!srcFile.exists()) {
            throw new SlimFixtureException(false, "Unable to find: " + srcFile.getAbsolutePath());
        }

        String fullTarget = getFullName(targetName);
        ensureParentExists(fullTarget);
        File destFile = FileUtil.copyFile(fullSource, fullTarget);
        return linkToFile(destFile);
    }

    private void ensureParentExists(String fullName) {
        File f = new File(fullName);
        File parentFile = f.getParentFile();
        parentFile.mkdirs();
    }

    private String getFullName(String filename) {
        String name;
        if (filename.startsWith(File.separator)
                || ":\\".equals(filename.substring(1, 3))) {
            name = filename;
        } else if (isFilesUrl(filename)){
            name = getFilePathFromWikiUrl(filename);
        } else {
            name = directory + filename;
        }
        return cleanupPath(name);
    }

    private boolean isFilesUrl(String filename) {
        String url = getUrl(filename);
        return !filename.equals(url) && url.startsWith("files/");
    }

    private String cleanupPath(String fullPath) {
        String clean;
        if ("\\".equals(File.separator)) {
            clean = fullPath.replaceAll("/", File.separator);
        } else {
            clean = fullPath.replaceAll("\\\\", File.separator);
        }
        return clean;
    }

    private String linkToFile(File f) {
        String url = getWikiUrl(f.getAbsolutePath());
        if (url == null) {
            url = f.toURI().toString();
        }
        return String.format("<a href=\"%s\">%s</a>", url, f.getName());
    }

}
