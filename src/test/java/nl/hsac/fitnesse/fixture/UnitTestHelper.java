package nl.hsac.fitnesse.fixture;

import fitnesse.components.PluginsClassLoader;
import fitnesse.junit.JUnitHelper;
import fitnesse.junit.JUnitXMLTestListener;
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

    /**
     * Creates helper to run Fitnesse inside junit execution
     * @return JUnitHelper.
     */
    public static JUnitHelper createFitnesseHelper() {
        try {
            new PluginsClassLoader().addPluginsToClassLoader();
        } catch (Exception e) {
            throw new RuntimeException("Unable to adds plugins to classpath", e);
        }
        // the paths supplied are relative to the working directory set in pom.xml (i.e. wiki)
        JUnitXMLTestListener resultsListener = new JUnitXMLTestListener("../target/failsafe-reports");
        JUnitHelper jUnitHelper = new JUnitHelper(".", "../target/fitnesse-results", resultsListener);
        return jUnitHelper;
    }
}
