package nl.hsac.fitnesse.fixture.slim.web;

import nl.hsac.fitnesse.fixture.util.NgClientSideScripts;

/**
 * Browser Test targeted to test AngularJs apps.
 */
public class NgBrowserTest extends BrowserTest {
    @Override
    public boolean open(String address) {
        boolean result = super.open(address);
        if (result) {
            waitForAngularRequestsToFinish();
        }
        return result;
    }

    public void waitForAngularRequestsToFinish() {
        waitForJavascriptCallback(NgClientSideScripts.WaitForAngular, "body");
    }
}
