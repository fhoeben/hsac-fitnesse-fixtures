package nl.hsac.fitnesse.fixture.slim.web;

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
        waitForJavascriptCallback("angular.element(document.body).injector().get('$browser').notifyWhenNoOutstandingRequests(callback);");
    }
}
