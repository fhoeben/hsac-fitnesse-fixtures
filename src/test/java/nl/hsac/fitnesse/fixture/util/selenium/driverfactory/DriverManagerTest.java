package nl.hsac.fitnesse.fixture.util.selenium.driverfactory;

import nl.hsac.fitnesse.fixture.slim.StopTestException;
import nl.hsac.fitnesse.fixture.util.selenium.SeleniumHelper;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.chrome.ChromeDriver;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DriverManagerTest {

    @Mock
    private LocalDriverFactory driverFactory;

    @Mock
    private ChromeDriver webDriver;

    @InjectMocks
    private DriverManager driverManager;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void getSeleniumHelperReturnsSeleniumHelper() {
        when(driverFactory.createDriver()).thenReturn(webDriver);

        SeleniumHelper result = driverManager.getSeleniumHelper();

        Assert.assertNotNull(result);
        Assert.assertEquals(result.driver(), webDriver);
    }

    @Test
    public void ifCreateDriverThrowsAnExceptionTheExceptionIsRethrownAsStopTestException() {
        String message = "driver is not compatible!";

        expectedEx.expect(StopTestException.class);
        expectedEx.expectMessage(message);

        when(driverFactory.createDriver()).thenThrow(new SessionNotCreatedException(message));

        driverManager.getSeleniumHelper();
    }
}
