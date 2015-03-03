package nl.hsac.fitnesse.fixture.slim.web;

import nl.hsac.fitnesse.fixture.util.JavascriptBy;
import nl.hsac.fitnesse.fixture.util.NgClientSideScripts;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Browser Test targeted to test AngularJs apps.
 */
public class NgBrowserTest extends BrowserTest {
    private String angularRoot = null;

    @Override
    public boolean open(String address) {
        boolean result = super.open(address);
        if (result) {
            waitForAngularRequestsToFinish();
        }
        return result;
    }

    public void waitForAngularRequestsToFinish() {
        String root = getAngularRoot();
        if (root == null) {
            root = "body";
        }
        waitForJavascriptCallback(NgClientSideScripts.WaitForAngular, root);
    }

    @Override
    protected void sendValue(WebElement element, String value) {
        waitForAngularRequestsToFinish();
        super.sendValue(element, value);
    }

    @Override
    public boolean click(String place) {
        waitForAngularRequestsToFinish();
        return super.click(place);
    }

    @Override
    public String valueFor(String place) {
        String result;
        waitForAngularRequestsToFinish();
        WebElement angularModelBinding = findByJavascript(NgClientSideScripts.FindBindings, place);
        if (angularModelBinding == null) {
            result = super.valueFor(place);
        } else {
            result = valueFor(angularModelBinding);
        }
        return result;
    }

    @Override
    public boolean selectFor(String value, String place) {
        boolean result;
        waitForAngularRequestsToFinish();
        WebElement angularModelSelect = findByJavascript(NgClientSideScripts.FindSelects, place);
        if (angularModelSelect == null) {
            result = super.selectFor(value, place);
        } else {
            result = clickSelectOption(angularModelSelect, value);
        }
        return result;
    }

    @Override
    public boolean enterAs(String value, String place) {
        boolean result;
        waitForAngularRequestsToFinish();
        WebElement angularModelInput = findByJavascript(NgClientSideScripts.FindInputs, place);
        if (angularModelInput == null) {
            result = super.enterAs(value, place);
        } else {
            angularModelInput.clear();
            sendValue(angularModelInput, value);
            result = true;
        }
        return result;
    }

    protected WebElement findByJavascript(String script, Object... parameters) {
        By by = new JavascriptBy(getAngularRoot(), script, parameters);
        return getSeleniumHelper().findElement(by);
    }

    public String getAngularRoot() {
        return angularRoot;
    }

    public void setAngularRoot(String anAngularRoot) {
        angularRoot = anAngularRoot;
    }
}
