package nl.hsac.fitnesse.junit;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Runs JUnit tests from the command line, with RunListeners attached.
 */
public class JUnitConsoleRunner {
    protected List<RunListener> getListeners() {
        return Arrays.asList(new JUnitXMLPerPageListener(), new ProgressLoggerListener());
    }

    protected List<Class> getTestClasses(String[] args) {
        List<Class> classes = new ArrayList<>();
        for (String arg : args) {
            try {
                classes.add(Class.forName(arg));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Unable to create instance of: " + arg);
            }
        }
        return classes;
    }

    public Result runTests(String... args) {
        JUnitCore core = new JUnitCore();
        for (RunListener r : getListeners()) {
            core.addListener(r);
        }
        List<Class> classes = getTestClasses(args);
        return core.run(classes.toArray(new Class[classes.size()]));
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("No test class names provided as argument.");
            System.exit(-1);
        }
        Result r = new JUnitConsoleRunner().runTests(args);
        System.exit(r.wasSuccessful()? 0 : 1);
    }
}
