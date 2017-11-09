package nl.hsac.fitnesse.fixture.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LineEndingHelperTest {
    private LineEndingHelper helper = new LineEndingHelper();
    private String unixVersion = FileUtil.loadFile("line_endings/unix-file.txt");
    private String winVersion = FileUtil.loadFile("line_endings/windows-file.txt");

    @Test
    public void testUnixToWindows() {
        String result = helper.convertEndingsTo(unixVersion, "\r\n");
        assertEquals(winVersion, result);
    }

    @Test
    public void testWindowsToWindows() {
        String result = helper.convertEndingsTo(winVersion, "\r\n");
        assertEquals(winVersion, result);
    }

    @Test
    public void testWindowsToUnix() {
        String result = helper.convertEndingsTo(winVersion, "\n");
        assertEquals(unixVersion, result);
    }

    @Test
    public void testUnixToUnix() {
        String result = helper.convertEndingsTo(unixVersion, "\n");
        assertEquals(unixVersion, result);
    }
}
