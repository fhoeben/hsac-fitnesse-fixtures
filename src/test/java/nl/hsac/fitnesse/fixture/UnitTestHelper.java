package nl.hsac.fitnesse.fixture;

import nl.hsac.fitnesse.fixture.util.FileUtil;
import nl.hsac.fitnesse.fixture.util.XmlHttpResponse;

/**
 * Helper for unit tests.
 */
public class UnitTestHelper {
    public static void fillResponse(XmlHttpResponse response, String filename) {
        response.setResponse(FileUtil.loadFile(filename));
        Environment.getInstance().setContext(response);
    }
}
