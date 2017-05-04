package nl.hsac.fitnesse.fixture.slim.web;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SeleniumDriverSetupTest {
    private final SeleniumDriverSetup fixture = new SeleniumDriverSetup();

    @Ignore("No executables downloaded for Linux, so skip in Travis-CI")
    @Test
    public void testFindChromedriverExecutable() {
        String exec = fixture.getExecutable("chromedriver");

        assertNotNull("No executable found for chromedriver", exec);
        File execFile = new File(exec);
        assertTrue("Executable for chromedriver does not exist: " + exec, execFile.exists());
        assertTrue("Executable for chromedriver not executable: " + exec, execFile.canExecute());
    }

}