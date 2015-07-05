package nl.hsac.fitnesse.fixture.slim;

import nl.hsac.fitnesse.fixture.slim.web.BrowserTest;
import nl.hsac.fitnesse.fixture.slim.web.SeleniumDriverSetup;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class FixtureFactoryTest {
    final private FixtureFactory fixtureFactory = new FixtureFactory();

    @Before
    public void setUp() throws Exception {
        SeleniumDriverSetup setup = new SeleniumDriverSetup();
        setup.startDriverFor("chrome");
    }

    @After
    public void tearDown() {
        SeleniumDriverSetup setup = new SeleniumDriverSetup();
        setup.stopDriver();
    }

    @Test
    public void testBrowserTest() {
        BrowserTest test = fixtureFactory.create(BrowserTest.class);
        assertTrue(test.open("http://www.funda.nl"));
        assertTrue(test.enterAs("Maliebaan, Utrecht", "Locatie"));
        assertTrue(test.click("Zoeken"));
        assertTrue(test.click("Maliebaan 50 A"));
        assertEquals("http://www.funda.nl/koop/utrecht/huis-47216986-maliebaan-50-a/", test.location());

    }

    @Test
    public void testBrowserTestWithTimeout() {
        BrowserTest test = fixtureFactory.create(BrowserTest.class, new Class<?>[] {int.class}, new Object[] {15});
        assertTrue(test.open("http://www.funda.nl"));
        assertTrue(test.enterAs("Maliebaan, Utrecht", "Locatie"));
        assertTrue(test.click("Zoeken"));
        assertTrue(test.click("Maliebaan 50 A"));
        assertEquals("http://www.funda.nl/koop/utrecht/huis-47216986-maliebaan-50-a/", test.location());

    }

    @Test
    public void testBrowserTestWithTimeout2() {
        BrowserTest test = fixtureFactory.create(BrowserTest.class, 15);
        assertTrue(test.open("http://www.funda.nl"));
        assertTrue(test.enterAs("Maliebaan, Utrecht", "Locatie"));
        assertTrue(test.click("Zoeken"));
        assertTrue(test.click("Maliebaan 50 A"));
        assertEquals("http://www.funda.nl/koop/utrecht/huis-47216986-maliebaan-50-a/", test.location());

    }
}
