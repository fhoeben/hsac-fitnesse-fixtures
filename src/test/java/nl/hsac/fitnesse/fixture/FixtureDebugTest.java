package nl.hsac.fitnesse.fixture;

import fitnesse.junit.FitNesseRunner;
import nl.hsac.fitnesse.junit.HsacFitNesseRunner;
import org.junit.runner.RunWith;

/**
 * Test class to allow fixture code to be debugged.
 */
@RunWith(HsacFitNesseRunner.class)
@FitNesseRunner.Suite("HsacAcceptanceTests.SlimTests.BrowserTest.CountTextOccurrenceTest")
// Here we use an explicit FilesSectionCopy (without default) because we DO want to get galenExamples and httpPostExamples
@HsacFitNesseRunner.FilesSectionCopy(addDefaultExcludes = false,
        exclude = {"testResults", "testProgress",
                    "screenshots", "pagesources", "downloads", "galen-reports", "fileFixture",
                    "test", "Desktop.ini", ".DS_Store"})
public class FixtureDebugTest {
}
