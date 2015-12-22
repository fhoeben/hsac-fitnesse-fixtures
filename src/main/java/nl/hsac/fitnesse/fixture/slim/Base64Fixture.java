package nl.hsac.fitnesse.fixture.slim;

import nl.hsac.fitnesse.fixture.util.FileUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Fixture to deal with Base64 values.
 */
public class Base64Fixture extends SlimFixture {
    private String saveBase = new File(filesDir, "base64").getPath() + "/";

    /**
     * Gets the content of specified file base64 encoded.
     * @param fileUrl either absolute path, or link to file inside wiki's files section.
     * @return base64 encoded file content.
     */
    public String encode(String fileUrl) {
        String file = getFilePathFromWikiUrl(fileUrl);
        try {
            byte[] content = IOUtils.toByteArray(new FileInputStream(file));
            return base64Encode(content);
        } catch (IOException e) {
            throw new SlimFixtureException("Unable to read: " + file, e);
        }
    }

    /**
     * Creates a new file with content read from base64 encoded string.
     * @param fileName (base) file name to create (if a file with specified name already exists
     *                 a number will be added to make the name unique).
     * @param base64String base64 encoded string to decode and use as file content.
     * @return location of created file.
     */
    public String createFrom(String fileName, String base64String) {
        String result;
        String baseName = FilenameUtils.getBaseName(fileName);
        String target = saveBase + baseName;
        String ext = FilenameUtils.getExtension(fileName);
        byte[] content = base64Decode(base64String);
        String downloadedFile = FileUtil.saveToFile(target, ext, content);
        String wikiUrl = getWikiUrl(downloadedFile);
        if (wikiUrl != null) {
            // make href to file
            result = String.format("<a href=\"%s\">%s</a>", wikiUrl, fileName);
        } else {
            result = downloadedFile;
        }
        return result;
    }

    protected String base64Encode(byte[] content) {
        return org.apache.commons.codec.binary.Base64.encodeBase64String(content);
    }
    protected byte[] base64Decode(String base64String) {
        return org.apache.commons.codec.binary.Base64.decodeBase64(base64String);
    }

    public String getSaveBase() {
        return saveBase;
    }

    public void setSaveBase(String saveBase) {
        this.saveBase = saveBase;
    }
}
