package nl.hsac.fitnesse.fixture.slim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FileFixtureTest {
    private final FileFixture fixture = new FileFixture();
    private final String txtFilename = "testFileFixture.txt";
    private final String copyFilename = "temp-copy.txt";
    private final String curDir = Paths.get("").toAbsolutePath().toString();
    private final String defaultFixtureSubDir = FilenameUtils.separatorsToSystem("files/fileFixture");
    private final String testResourcesDir = FilenameUtils.separatorsToSystem(curDir + "/src/test/resources/");
    private final String testResourcesDirUri = new File(testResourcesDir).toURI().toString();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testGetAndSetDirectory() {
        assertTrue(fixture.getDirectory().contains(defaultFixtureSubDir));
        fixture.setDirectory(testResourcesDir);
        assertEquals(testResourcesDir, fixture.getDirectory());
    }

    @Test
    public void testExists() {
        fixture.setDirectory(testResourcesDir);
        assertTrue(fixture.exists(txtFilename));
        assertFalse(fixture.exists("foobar"));
    }

    /**
     * Test testIn successful.
     * @throws IOException on failure
     */
    @Test
    public void testTextInOk() throws IOException {
        fixture.setDirectory(testResourcesDir);
        assertEquals("# Expected as first line of text.\n# Expected as 2nd line of text.", fixture.textIn(txtFilename));

    }

    /**
     * Test textIn unsuccessful (file doesn't exist).
     * @throws IOException on failure
     */
    @Test
    public void testTextInNok() throws IOException {
        String fileNameNonExisting = "foobar";
        fixture.setDirectory(testResourcesDir);

        thrown.expect(SlimFixtureException.class);
        thrown.expectMessage("message:<<Unable to find: " + testResourcesDir + fileNameNonExisting + ">>");
        fixture.textIn(fileNameNonExisting);
    }

    @Test
    public void testDeleteIfExists() throws IOException {
        // File exists.
        FileUtils.touch(new File(testResourcesDir + copyFilename));
        fixture.setDirectory(testResourcesDir);
        fixture.deleteIfExists(copyFilename);

        assertFalse(new File(testResourcesDir + copyFilename).exists());

        // File does not exist.
        fixture.deleteIfExists(copyFilename);
    }

    @Test
    public void testCopyTo() throws IOException {
        fixture.setDirectory(testResourcesDir);
        assertTrue(fixture.exists(txtFilename));
        String res = fixture.copyTo(txtFilename, copyFilename);
        assertEquals("<a href=\"" + testResourcesDirUri + copyFilename + "\">" + copyFilename + "</a>", res);
        assertTrue(fixture.exists(copyFilename));
    }

    @Test
    public void testTakeFirstLineFrom() throws IOException {
        fixture.setDirectory(testResourcesDir);
        fixture.copyTo(txtFilename, copyFilename);
        assertEquals("# Expected as first line of text.",
                fixture.takeFirstLineFrom(copyFilename));
        assertEquals("# Expected as 2nd line of text.",
                fixture.takeFirstLineFrom(copyFilename));
    }

    @Test
    public void testAppendContainingOnNewline() throws IOException {
        fixture.setDirectory(testResourcesDir);
        fixture.copyTo(txtFilename, copyFilename);
        String res = fixture.appendToOnNewLine("Third line", copyFilename);
        assertEquals("<a href=\"" + testResourcesDirUri + copyFilename + "\">" + copyFilename + "</a>", res);
        assertEquals(
                String.format("# Expected as first line of text.\n# Expected as 2nd line of text.%sThird line", System.lineSeparator()),
                fixture.textIn(copyFilename));

    }

    @Test
    public void testAppendContainingOnSameline() throws IOException {
        fixture.setDirectory(testResourcesDir);
        fixture.copyTo(txtFilename, copyFilename);
        String res = fixture.appendTo("Third line", copyFilename);
        assertEquals("<a href=\"" + testResourcesDirUri + copyFilename + "\">" + copyFilename + "</a>", res);
        assertEquals("# Expected as first line of text.\n# Expected as 2nd line of text.Third line",
                fixture.textIn(copyFilename));
    }

    @Test
    public void testDelete() throws IOException {
        FileUtils.touch(new File(testResourcesDir + copyFilename));
        fixture.setDirectory(testResourcesDir);
        fixture.delete(copyFilename);
    }

    /**
     * Cleanup. Doing forceDelete so exception is thrown when if fails. Can be indication some filehandler is not release
     * properly
     * @throws IOException
     */
    @After
    public void tearDown() throws IOException {
        File file = new File(testResourcesDir + copyFilename);
        if (file.exists()) {
            FileUtils.forceDelete(new File(testResourcesDir + copyFilename));
        }
    }
}
