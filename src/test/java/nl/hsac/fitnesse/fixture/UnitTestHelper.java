package nl.hsac.fitnesse.fixture;

import fitnesse.junit.JUnitHelper;
import fitnesse.junit.JUnitXMLTestListener;
import fitnesse.responders.run.ResultsListener;
import java.io.IOException;
import java.net.ServerSocket;
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
        // the paths supplied are relative to the working directory set in pom.xml (i.e. wiki)
        ResultsListener resultsListener = new JUnitXMLTestListener("../target/failsafe-reports");
        JUnitHelper jUnitHelper = new JUnitHelper(".", "../target/fitnesse-results", resultsListener);
        // FIT needs a local free port
        int localPort = getRandomFreePort();
        jUnitHelper.setPort(localPort);
        return jUnitHelper;
    }
    
    private static int getRandomFreePort() {
        int localPort = 80;
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(0);
            localPort = socket.getLocalPort();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // just ignore
                }
            }
        }
        return localPort;
    }
}
