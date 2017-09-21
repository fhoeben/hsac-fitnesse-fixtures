package nl.hsac.fitnesse.fixture.slim.web;

import nl.hsac.fitnesse.fixture.util.selenium.Ng2ClientSideScripts;

/**
 * Browser Test targeted to test Angular2+ apps.
 */
public class Ng2BrowserTest extends NgBrowserTestBase {

    public Ng2BrowserTest() {
        super(Ng2ClientSideScripts.WaitForAngular);
    }

    public Ng2BrowserTest(int secondsBeforeTimeout) {
        super(secondsBeforeTimeout, Ng2ClientSideScripts.WaitForAngular);
    }

}
