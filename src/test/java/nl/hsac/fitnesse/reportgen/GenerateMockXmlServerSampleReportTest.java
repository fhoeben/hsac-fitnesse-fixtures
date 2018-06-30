package nl.hsac.fitnesse.reportgen;

import fitnesse.junit.FitNesseRunner;
import nl.hsac.fitnesse.junit.HsacFitNesseRunner;
import org.junit.Ignore;
import org.junit.runner.RunWith;

@Ignore("Only run when want to generate new html reports, FitNesse upgrade")
@RunWith(HsacFitNesseRunner.class)
@FitNesseRunner.Suite("HsacAcceptanceTests.SlimTests.MockXmlServerTest")
@FitNesseRunner.OutputDir("src/test/resources/htmlReports/MockXmlServerTest")
public class GenerateMockXmlServerSampleReportTest {
}
