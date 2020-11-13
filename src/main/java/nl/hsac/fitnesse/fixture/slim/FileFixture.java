package nl.hsac.fitnesse.fixture.slim;

import nl.hsac.fitnesse.fixture.util.FileUtil;
import nl.hsac.fitnesse.fixture.util.LineEndingHelper;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

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
            if (new File(aDirectory).isAbsolute()) {
                directory = aDirectory;
            } else {
                // relative directories are relative to the FitNesseDir
                directory = new File(getEnvironment().getFitNesseDir(), aDirectory).getAbsolutePath();
            }
        }
        if (!directory.endsWith(File.separator)) {
            directory += File.separator;
        }
    }

    public String getDirectory(){
        return directory;
    }

    public String createFileFromBase64(String baseName, String base64Content) {
        Base64Fixture base64Fixture = getBase64Fixture();
        byte[] bytes = base64Fixture.base64Decode(base64Content);
        return createContaining(baseName, bytes);
    }

    /**
     * Creates new file, containing value 'key'.
     * @param filename name of file to create.
     * @param key key whose value should be used to generate the file.
     * @return file created.
     */
    public String createContainingValue(String filename, String key) {
        Object data = value(key);
        if (data == null) {
            throw new SlimFixtureException(false, "No value for key: " + key);
        }
        return createContaining(filename, data);
    }

    public String createContaining(String filename, Object data) {
        String file;
        if (data instanceof byte[]) {
            file = createContaining(filename, (byte[]) data);
        } else if (data != null) {
            file = createContaining(filename, data.toString());
        } else {
            throw new SlimFixtureException(false, "Unable to create file from null");
        }
        return file;
    }

    public String createContaining(String filename, String content) {
        String fullName = getFullName(filename);
        ensureParentExists(fullName);
        File f = FileUtil.writeFile(fullName, content);
        return linkToFile(f);
    }

    protected String createContaining(String filename, byte[] content) {
        String baseName = FilenameUtils.getBaseName(filename);
        String target = getFullName(baseName);
        ensureParentExists(target);
        String ext = FilenameUtils.getExtension(filename);
        String downloadedFile = FileUtil.saveToFile(target, ext, content);
        return linkToFile(downloadedFile);
    }

    public String textIn(String filename) {
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

    public boolean convertLineEndingsOfToWindows(String filename) throws IOException {
        return convertLineEndingsOf(filename, LineEndingHelper.WIN_LINE_ENDING);
    }

    public boolean convertLineEndingsOfToUnix(String filename) throws IOException {
        return convertLineEndingsOf(filename, LineEndingHelper.UNIX_LINE_ENDING);
    }

    protected boolean convertLineEndingsOf(String filename, String lineEnding) throws IOException {
        String fullName = getFullName(filename);

        String original = textIn(fullName);
        String converted = getEnvironment().getLineEndingHelper().convertEndingsTo(original, lineEnding);

        //Create a temporary new file, then delete the original and copy temp file to original filename
        String tmpFilename = fullName + ".tmp";
        File tmpFile = FileUtil.writeFile(tmpFilename, converted);
        FileUtil.copyFile(tmpFilename, fullName);
        tmpFile.delete();

        return true;
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

    public boolean exists(String filename) {
        String fullName = getFullName(filename);
        return new File(fullName).exists();
    }

    public boolean delete(String filename) {
        String fullName = getFullName(filename);
        File file = getFile(fullName);
        return file.delete();
    }

    public boolean deleteIfExists(String filename) {
        String fullName = getFullName(filename);
        File file = new File(fullName);
        boolean result = file.exists();
        if (result) {
            if (!file.delete()) {
                throw new SlimFixtureException(false, "Unable to delete file");
            }
        }
        return result;
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
            String url = getUrl(filename);
            if (url.startsWith("file:")) {
                name = url.substring(5);
            } else {
                name = directory + filename;
            }
        }
        return cleanupPath(name);
    }

    private boolean isFilesUrl(String filename) {
        String url = getUrl(filename);
        return (!filename.equals(url) && url.startsWith("files/")) || url.startsWith("http://files");
    }

    private String cleanupPath(String fullPath) {
        return FilenameUtils.separatorsToSystem(fullPath);
    }

    public boolean pollUntilExists(String filename) {
        String fullname = getFullName(filename);
        return repeatUntil(fileExistsCompletion(fullname));
    }

    public boolean pollUntilDoesNotExist(String filename) {
        String fullname = getFullName(filename);
        return repeatUntilNot(fileExistsCompletion(fullname));
    }

    protected static FunctionalCompletion fileExistsCompletion(String fullname) {
        return new FunctionalCompletion(() -> new File(fullname).exists());
    }

    public boolean pollUntilSizeOfExceeds(String filename, long expectedSize) {
        return repeatUntil(new FunctionalCompletion(() -> exists(filename) && sizeOf(filename) > expectedSize));
    }

    public String appendTo(String extraContent, String filename) {
        String fullName = getFullName(filename);
        ensureParentExists(fullName);
        File f = FileUtil.appendToFile(fullName, extraContent, false);
        return linkToFile(f);
    }

    public String appendToOnNewLine(String extraContent, String filename) {
        String fullName = getFullName(filename);
        ensureParentExists(fullName);
        File f = FileUtil.appendToFile(fullName, extraContent, true);
        return linkToFile(f);
    }

    public String zipAs(String inputfile, String zipname) {
        String fullName = getFullName(inputfile);
        String[] inputFiles;

        File inputSrc = new File(fullName);
        if (inputSrc.isDirectory()) {
            inputFiles = Arrays.stream(inputSrc.listFiles())
                    .map(File::getAbsolutePath)
                    .toArray(String[]::new);
        } else {
            inputFiles = new String[] { fullName };
        }

        try {
            String nonExistingZip = findNameForFile(zipname);
            File zip = getEnvironment().getZipHelper().createZip(nonExistingZip, inputFiles);
            return linkToFile(zip);
        } catch (IOException e) {
            throw new SlimFixtureException(true, "Unable to create zip. " + e.getMessage(), e);
        }
    }

    public String unzipAs(String zipname, String targetDir) {
        String zipfile = getFullName(zipname);

        try {
            String nonExistingTarget = findNameForFile(targetDir);
            getEnvironment().getZipHelper().unzip(zipfile, nonExistingTarget);
            return linkToFile(nonExistingTarget);
        } catch (IOException e) {
            throw new SlimFixtureException(true, "Unable to unzip. " + e.getMessage(), e);
        }

    }

    private String findNameForFile(String fileFromWiki) {
        String basename = FilenameUtils.getBaseName(fileFromWiki);
        String target = getFullName(basename);
        ensureParentExists(target);
        String targetExt = FilenameUtils.getExtension(fileFromWiki);

        return FileUtil.determineFilename(target, targetExt).getAbsolutePath();
    }

    public long numberOfFilesInZip(String zipname) {
        return applyOnZipEntries(zipname, Stream::count);
    }

    public ArrayList<String> namesOfFilesInZip(String zipname) {
        return applyOnZipEntries(zipname,
                s -> s.map(ZipEntry::getName)
                        .collect(Collectors.toCollection(ArrayList::new)));
    }

    protected <T> T applyOnZipEntries(String zipname, Function<Stream<ZipEntry>, T> function) {
        String zipfile = getFullName(zipname);

        try {
            List<ZipEntry> entries = getEnvironment().getZipHelper().getEntries(zipfile);
            return function.apply(entries.stream()
                    .filter(e -> !e.isDirectory()));
        } catch (IOException e) {
            throw new SlimFixtureException(true, "Unable get zip entries. " + e.getMessage(), e);
        }
    }

}
