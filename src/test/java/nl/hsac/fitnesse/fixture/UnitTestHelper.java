package nl.hsac.fitnesse.fixture;

import nl.hsac.fitnesse.fixture.util.FileUtil;
import nl.hsac.fitnesse.fixture.util.XmlHttpResponse;

import javax.xml.namespace.NamespaceContext;

/**
 * Helper for unit tests.
 */
public class UnitTestHelper {
    private static final NamespaceContext NS_CONTEXT = Environment.getInstance().getNamespaceContext();
    
    public static void fillResponse(XmlHttpResponse response, String filename) {
        response.setResponse(FileUtil.loadFile(filename));
        response.setNamespaceContext(NS_CONTEXT);
    }
}
