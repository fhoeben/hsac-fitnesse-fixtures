package nl.hsac.fitnesse.fixture.util;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ZipHelperTest {
    private static final String TEST_ZIP_NAME = "target/test.zip";

    @Before
    public void setUp() {
        new File(TEST_ZIP_NAME).delete();
    }

    @Test
    public void willNotOverwriteZip() throws IOException {
        ZipHelper helper = new ZipHelper();

        File existingFile = new File(TEST_ZIP_NAME);
        assertTrue(existingFile.createNewFile());
        String f1 = "src/test/resources/GetWeatherSoapResponse.xml";

        try {
            File zip = helper.createZip(TEST_ZIP_NAME, f1);
            fail("Expected exception, got: " + zip.getAbsolutePath());
        } catch (IOException e) {
            assertEquals("File already exists: " + existingFile.getAbsolutePath(), e.getMessage());
        }
    }

    @Test
    public void roundTrip() throws IOException {
        ZipHelper helper = new ZipHelper();

        String f1 = "src/test/resources/GetWeatherSoapResponse.xml";
        String f2 = "src/test/resources/GetWeatherSoapResponseFormatted.xml";
        String f3 = "src/test/resources/line_endings";
        File zip = helper.createZip(TEST_ZIP_NAME, f1, f2, f3);

        assertTrue(zip.exists());
        assertTrue(zip.isFile());
        assertTrue(getFileSize(zip) > 0);
        assertEquals(new File(TEST_ZIP_NAME).getAbsolutePath(), zip.getAbsolutePath());

        String targetDir = "target/unzipTest";
        File unzipDir = new File(targetDir);
        assertTrue(FileUtil.delete(unzipDir));

        List<File> result = helper.unzip(TEST_ZIP_NAME, targetDir);
        assertEquals(4, result.size());
        assertFalse(result.contains(unzipDir));

        File[] files = unzipDir.getAbsoluteFile().listFiles();
        assertEquals(3, files.length);
        Arrays.sort(files);
        assertEquals("GetWeatherSoapResponse.xml", files[0].getName());
        assertEquals("GetWeatherSoapResponseFormatted.xml", files[1].getName());
        assertEquals("line_endings", files[2].getName());

        assertTrue(result.contains(files[0]));
        assertTrue(result.contains(files[1]));
        assertFalse(result.contains(files[2]));

        assertEquals(getFileSize(f1), getFileSize(files[0]));
        assertEquals(getFileSize(f2), getFileSize(files[1]));

        File[] files2 = files[2].listFiles();
        assertEquals(2, files2.length);
        Arrays.sort(files2);
        assertEquals("unix-file.txt", files2[0].getName());
        assertEquals("windows-file.txt", files2[1].getName());

        assertTrue(result.contains(files2[0]));
        assertTrue(result.contains(files2[1]));

        try {
            helper.unzip(TEST_ZIP_NAME, targetDir);
            fail("Expected exception");
        } catch (FileAlreadyExistsException e) {
            assertTrue(e.getMessage(), e.getMessage().startsWith(unzipDir.getAbsolutePath()));
        }

    }

    @Test
    public void listEntries() throws IOException {
        ZipHelper helper = new ZipHelper();

        String f1 = "src/test/resources/GetWeatherSoapResponse.xml";
        String f2 = "src/test/resources/GetWeatherSoapResponseFormatted.xml";
        String f3 = "src/test/resources/line_endings";
        File zip = helper.createZip(TEST_ZIP_NAME, f1, f2, f3);

        List<ZipEntry> entries = helper.getEntries(zip.getAbsolutePath());
        List<String> names = entries.stream()
                .map(ZipEntry::getName)
                .sorted().collect(Collectors.toList());

        assertEquals(5, entries.size());
        assertEquals("GetWeatherSoapResponse.xml", names.get(0));
        assertEquals("GetWeatherSoapResponseFormatted.xml", names.get(1));
        assertEquals("line_endings/", names.get(2));
        assertEquals("line_endings/unix-file.txt", names.get(3));
        assertEquals("line_endings/windows-file.txt", names.get(4));

        assertFalse(entries.get(0).isDirectory());
        assertFalse(entries.get(1).isDirectory());
        assertTrue(entries.get(2).isDirectory());
        assertFalse(entries.get(3).isDirectory());
        assertFalse(entries.get(4).isDirectory());

        assertEquals(getFileSize(f1), entries.get(0).getSize());
        assertEquals(getFileSize(f2), entries.get(1).getSize());
        assertEquals(0, entries.get(2).getSize());

        checkIsCompressed(entries.get(0));
        checkIsCompressed(entries.get(1));
        checkIsCompressed(entries.get(3));
        checkIsCompressed(entries.get(4));
    }

    private void checkIsCompressed(ZipEntry entry) {
        assertTrue("Not compressed: " + entry.getName(),
                entry.getSize() - entry.getCompressedSize() > 0);

    }

    private long getFileSize(String filename) {
        return getFileSize(new File(filename));
    }

    private long getFileSize(File file) {
        return file.length();
    }
}
