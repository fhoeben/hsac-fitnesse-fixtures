package nl.hsac.fitnesse.fixture.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class LineEndingHelperTest {
    private LineEndingHelper helper = new LineEndingHelper();
    private String unixVersion = FileUtil.loadFile("line_endings/unix-file.txt");
    private String winVersion = FileUtil.loadFile("line_endings/windows-file.txt");

    @Test
    public void filesAreDifferent() {
        assertNotEquals(winVersion, unixVersion);
    }

    @Test
    public void testUnixToWindows() {
        String result = helper.convertEndingsTo(unixVersion, LineEndingHelper.WIN_LINE_ENDING);
        assertEquals(winVersion, result);
    }

    @Test
    public void testWindowsToWindows() {
        String result = helper.convertEndingsTo(winVersion, LineEndingHelper.WIN_LINE_ENDING);
        assertEquals(winVersion, result);
    }

    @Test
    public void testWindowsToUnix() {
        String result = helper.convertEndingsTo(winVersion, LineEndingHelper.UNIX_LINE_ENDING);
        assertEquals(unixVersion, result);
    }

    @Test
    public void testUnixToUnix() {
        String result = helper.convertEndingsTo(unixVersion, LineEndingHelper.UNIX_LINE_ENDING);
        assertEquals(unixVersion, result);
    }
}
