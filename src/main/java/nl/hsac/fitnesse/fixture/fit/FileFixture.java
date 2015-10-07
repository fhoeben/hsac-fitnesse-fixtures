/**
 *
 */
package nl.hsac.fitnesse.fixture.fit;

import java.io.File;

/**
 * Example fixture on the usage of getFilePathFromWikiUrl for using files from wiki's files section or from local file system.
 */
public class FileFixture extends MapColumnFixture {
    private static final String FILENAME = "filename";

    public long fileSize() {
        String file = getFilePathFromWikiUrl(filename());
        return new File(file).length();
    }
    /**
     * @return the filename
     */
    public String filename() {
        return (String) getCurrentRowValues().get(FILENAME);
    }



}
