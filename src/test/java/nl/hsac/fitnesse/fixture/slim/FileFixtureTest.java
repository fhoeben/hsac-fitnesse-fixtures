package nl.hsac.fitnesse.fixture.slim;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class FileFixtureTest {
    private final FileFixture fixture = new FileFixture();
    private final String txtFilename = "testFileFixture.txt";
    private final String copyFilename = "temp-copy.txt";
    private final String curDir = Paths.get("").toAbsolutePath().toString();
    private final String defaultFilesDir = curDir + File.separator + "FitNesseRoot" + File.separator + "files";
    private final String defaultFixtureDir = defaultFilesDir + File.separator + "fileFixture" + File.separator;
    private final String testResourcesDir = curDir + "/src/test/resources/";

    @Test
    public void testGetAndSetDirectory() {
        assertEquals(defaultFixtureDir, fixture.getDirectory());
        fixture.setDirectory(testResourcesDir);
        assertEquals(testResourcesDir, fixture.getDirectory());
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
        } catch (IOException ioe) {
            fail("Should not happen.");
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
            assertEquals("<a href=\"file:" + testResourcesDir + copyFilename + "\">" + copyFilename + "</a>", res);
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
            assertEquals("<a href=\"file:" + testResourcesDir + copyFilename + "\">" + copyFilename + "</a>", res);
            assertEquals("# Expected as first line of text.\n# Expected as 2nd line of text.\nThird line",
                    fixture.textIn(copyFilename));
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
            assertEquals("<a href=\"file:" + testResourcesDir + copyFilename + "\">" + copyFilename + "</a>", res);
            assertEquals("# Expected as first line of text.\n# Expected as 2nd line of text.Third line",
                    fixture.textIn(copyFilename));
        } catch (IOException ioe) {
            fail("Should not happen: " + ioe.getMessage());
        }
    }

    @Test
    public void testDelete() {
        try {
            fixture.setDirectory(testResourcesDir);
            fixture.delete(copyFilename);
        } catch (SlimFixtureException sfe) {
            fail("Should not happen: " + sfe.getMessage());
        }
    }
}
