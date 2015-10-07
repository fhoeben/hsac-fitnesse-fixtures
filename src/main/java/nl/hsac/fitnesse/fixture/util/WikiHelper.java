/**
 *
 */
package nl.hsac.fitnesse.fixture.util;

import java.io.File;

/**
 * Helper for wiki stuff.
 */
public class WikiHelper {
    private HtmlCleaner htmlCleaner;
    private String filesDir;

    /**
     * @param aHtmlCleaner
     * @param aFilesDir
     */
    public WikiHelper(HtmlCleaner aHtmlCleaner, String aFilesDir) {
        super();
        htmlCleaner = aHtmlCleaner;
        filesDir = aFilesDir;
    }

    /**
     * @return the filesDir
     */
    public String getFilesDir() {
        return filesDir;
    }

    /**
     * Converts a file path into a relative wiki path, if the path is insides the wiki's 'files' section.
     * @param filePath path to file.
     * @return relative URL pointing to the file (so a hyperlink to it can be created).
     */
    public String getWikiUrl(String filePath) {
        String wikiUrl = null;
        if (filePath.startsWith(filesDir)) {
            String relativeFile = filePath.substring(filesDir.length());
            relativeFile = relativeFile.replace('\\', '/');
            wikiUrl = "files" + relativeFile;
        }
        return wikiUrl;
    }

    /**
     * Gets absolute path from wiki url, if file exists.
     * @param wikiUrl a relative path that can be used in wiki page, or any file path.
     * @return absolute path to the target of the url, if such a file exists; null if the target does not exist.
     */
    public String getFilePathFromWikiUrl(String wikiUrl) {
        String url = htmlCleaner.getUrl(wikiUrl);
        File file;
        if (url.startsWith("files/")) {
            String relativeFile = url.substring("files".length());
            relativeFile = relativeFile.replace('/', File.separatorChar);
            String pathname = filesDir + relativeFile;
            file = new File(pathname);
        } else {
            file = new File(url);
        }
        return file.exists() ? file.getAbsolutePath() : url;
    }
}
