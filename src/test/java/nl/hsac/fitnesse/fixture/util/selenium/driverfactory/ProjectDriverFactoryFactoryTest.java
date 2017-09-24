package nl.hsac.fitnesse.fixture.util.selenium.driverfactory;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ProjectDriverFactoryFactoryTest {
    private final ProjectDriverFactoryFactory factoryFactory = new ProjectDriverFactoryFactory();

    @Test
    public void testFindChromedriverExecutable() {
        String exec = factoryFactory.getExecutable("chromedriver");

        assertNotNull("No executable found for chromedriver", exec);
        File execFile = new File(exec);
        assertTrue("Executable for chromedriver does not exist: " + exec, execFile.exists());
        assertTrue("Executable for chromedriver not executable: " + exec, execFile.canExecute());
    }

}