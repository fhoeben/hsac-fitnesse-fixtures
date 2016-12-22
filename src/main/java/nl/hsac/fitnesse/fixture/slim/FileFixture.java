package nl.hsac.fitnesse.fixture.slim;

import nl.hsac.fitnesse.fixture.util.FileUtil;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.util.Scanner;

/**
 * Utility fixture to work with files.
 */
public class FileFixture extends SlimFixtureWithMap {
    private String directory = new File(filesDir, "fileFixture").getPath() + File.separator;

    public void setDirectory(String aDirectory) {
        if (isFilesUrl(aDirectory)) {
            String url = getUrl(aDirectory);
            String relativeDir = url.substring("files".length());
            relativeDir = relativeDir.replace('/', File.separatorChar);
            directory = filesDir + relativeDir;
        } else {
            directory = aDirectory;
        }
        if (!directory.endsWith(File.separator)) {
            directory += File.separator;
        }
    }

    public String createContaining(String filename, String content) {
        String fullName = getFullName(filename);
        ensureParentExists(fullName);
        File f = FileUtil.writeFile(fullName, content);
        return linkToFile(f);
    }

    public String textIn(String filename) throws IOException {
        String text = "";
        String fullName = getFullName(filename);
        if (sizeOf(filename) > 0) {
            try {
                FileInputStream s = new FileInputStream(fullName);
                text = FileUtil.streamToString(s, fullName);
            } catch (FileNotFoundException e) {
                throw new SlimFixtureException(false, "Unable to find: " + fullName);
            }
        }
        return text;
    }

    public String takeFirstLineFrom(String filename) throws IOException {
        String result;
        String fullName = getFullName(filename);
        ensureParentExists(fullName);
        File file = new File(fullName);
        Scanner fileScanner = new Scanner(file);
        if (fileScanner.hasNextLine()) {
            result = fileScanner.nextLine();
        } else {
            throw new IOException(fullName + " is an empty file.");
        }

        //Create a temporary new file, then delete the original and copy temp file to original filename
        String tmpFilename = fullName + ".tmp";
        File tmpFile = FileUtil.writeFromScanner(tmpFilename, fileScanner);
        FileUtil.copyFile(tmpFilename, fullName);
        tmpFile.delete();

        return result;
    }

    public String contentOf(String filename) throws IOException {
        String content = textIn(filename);
        return getEnvironment().getHtml(content);
    }

    public String createUsing(String filename, String templateName) {
        String content = getEnvironment().processTemplate(templateName, getCurrentValues());
        return createContaining(filename, content);
    }

    public String copyTo(String sourceName, String targetName) throws IOException {
        String fullSource = getFullName(sourceName);
        // ensure file exists
        getFile(fullSource);

        String fullTarget = getFullName(targetName);
        ensureParentExists(fullTarget);
        File destFile = FileUtil.copyFile(fullSource, fullTarget);
        return linkToFile(destFile);
    }

    public long sizeOf(String filename) {
        String fullName = getFullName(filename);
        File file = getFile(fullName);
        return file.length();
    }

    public String filenameOf(String filename) {
        String fullName = getFullName(filename);
        return getFile(fullName).getName();
    }

    public String baseNameOf(String filename) {
        return FilenameUtils.getBaseName(filenameOf(filename));
    }

    public String extensionOf(String filename) {
        return FilenameUtils.getExtension(filenameOf(filename));
    }

    protected File getFile(String fullName) {
        File file = new File(fullName);
        if (!file.exists()) {
            throw new SlimFixtureException(false, "Unable to find: " + file.getAbsolutePath());
        }
        return file;
    }

    protected void ensureParentExists(String fullName) {
        File f = new File(fullName);
        File parentFile = f.getParentFile();
        parentFile.mkdirs();
    }

    protected String getFullName(String filename) {
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
        return FilenameUtils.separatorsToSystem(fullPath);
    }

    protected String linkToFile(File f) {
        String url = getWikiUrl(f.getAbsolutePath());
        if (url == null) {
            url = f.toURI().toString();
        }
        return String.format("<a href=\"%s\">%s</a>", url, f.getName());
    }

}
