package nl.hsac.fitnesse.fixture.slim;

import nl.hsac.fitnesse.fixture.Environment;
import nl.hsac.fitnesse.fixture.util.LineEndingHelper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FileFixtureTest {
    private final FileFixture fixture = new FileFixture();
    private final String txtFilename = "testFileFixture.txt";
    private final String copyFilename = "temp-copy.txt";
    private final String curDir = Paths.get("").toAbsolutePath().toString();
    private final String testResourcesDir = curDir + "/src/test/resources/".replace('/', File.separatorChar);

    @Test
    public void testGetAndSetDirectory() {
        String defaultFilesDir = Environment.getInstance().getFitNesseFilesSectionDir();
        String defaultFixtureDir = defaultFilesDir + File.separator + "fileFixture" + File.separator;
        assertEquals(defaultFixtureDir, fixture.getDirectory());

        fixture.setDirectory(testResourcesDir);
        assertEquals(testResourcesDir, fixture.getDirectory());
    }

    @Test
    public void testGetAndSetDirectoryRelative() {
        String childDir = "bla";
        fixture.setDirectory(childDir);
        String fitNesseDir = Environment.getInstance().getFitNesseDir();
        String expected = new File(fitNesseDir, childDir).getAbsolutePath() + File.separator;
        assertEquals(expected, fixture.getDirectory());
    }

    @Test
    public void testExists() {
        fixture.setDirectory(testResourcesDir);
        assertTrue(fixture.exists(txtFilename));
        assertFalse(fixture.exists("foobar"));
    }

    @Test
    public void testTextIn() {
        try {
            fixture.setDirectory(testResourcesDir);
            assertEquals("# Expected as first line of text.\n# Expected as 2nd line of text.",
                    fixture.textIn(txtFilename));
            fixture.textIn("foobar");
        } catch (SlimFixtureException sfe) {
            assertEquals("message:<<Unable to find: " + testResourcesDir + "foobar>>", sfe.getMessage());
        }
    }

    @Test
    public void testDeleteIfExists() {
        try {
            fixture.setDirectory(testResourcesDir);
            if (fixture.exists(copyFilename)) {
                fixture.deleteIfExists(copyFilename);
            }
        } catch (SlimFixtureException sfe) {
            fail("Should not happen: " + sfe.getMessage());
        }
    }

    @Test
    public void testCopyTo() {
        fixture.setDirectory(testResourcesDir);
        assertTrue(fixture.exists(txtFilename));
        try {
            String res = fixture.copyTo(txtFilename, copyFilename);
            String expectedPath = getExpectedUrlPath();
            assertEquals("<a href=\"file:" + expectedPath + "\" target=\"_blank\">" + copyFilename + "</a>", res);
        } catch (IOException ioe) {
            fail("Should not happen: " + ioe.getMessage());
        }
        assertTrue(fixture.exists(copyFilename));
    }

    @Test
    public void testTakeFirstLineFrom() {
        try {
            fixture.setDirectory(testResourcesDir);
            fixture.copyTo(txtFilename, copyFilename);
            assertEquals("# Expected as first line of text.",
                    fixture.takeFirstLineFrom(copyFilename));
            assertEquals("# Expected as 2nd line of text.",
                    fixture.takeFirstLineFrom(copyFilename));
            fixture.textIn("foobar");
        } catch (SlimFixtureException sfe) {
            assertEquals("message:<<Unable to find: " + testResourcesDir + "foobar>>", sfe.getMessage());
        } catch (IOException ioe) {
            fail("Should not happen: " + ioe.getMessage());
        }
    }

    @Test
    public void testAppendContainingOnNewline() {
        try {
            fixture.setDirectory(testResourcesDir);
            fixture.copyTo(txtFilename, copyFilename);
            String res = fixture.appendToOnNewLine("Third line", copyFilename);
            String expectedPath = getExpectedUrlPath();
            assertEquals("<a href=\"file:" + expectedPath + "\" target=\"_blank\">" + copyFilename + "</a>", res);
            assertEquals("# Expected as first line of text.\n# Expected as 2nd line of text.\nThird line",
                    new LineEndingHelper().convertEndingsTo(fixture.textIn(copyFilename), "\n"));
        } catch (IOException ioe) {
            fail("Should not happen: " + ioe.getMessage());
        }
    }

    @Test
    public void testAppendContainingOnSameline() {
        try {
            fixture.setDirectory(testResourcesDir);
            fixture.copyTo(txtFilename, copyFilename);
            String res = fixture.appendTo("Third line", copyFilename);
            String expectedPath = getExpectedUrlPath();
            assertEquals("<a href=\"file:" + expectedPath + "\" target=\"_blank\">" + copyFilename + "</a>", res);
            assertEquals("# Expected as first line of text.\n# Expected as 2nd line of text.Third line",
                    fixture.textIn(copyFilename));
        } catch (IOException ioe) {
            fail("Should not happen: " + ioe.getMessage());
        }
    }

    private String getExpectedUrlPath() {
        String path = testResourcesDir + copyFilename;
        String unixStylePath = path.replace(File.separatorChar, '/');
        if (!unixStylePath.startsWith("/")) {
            unixStylePath = "/" + unixStylePath ;
        }
        return unixStylePath;
    }

    @Test
    public void testDelete() {
        try {
            fixture.setDirectory(testResourcesDir);
            fixture.copyTo(txtFilename, copyFilename);
            fixture.delete(copyFilename);
        } catch (Exception ex) {
            fail("Should not happen: " + ex.getMessage());
        }
    }
}
