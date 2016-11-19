package nl.hsac.fitnesse.fixture.slim.web;

import nl.hsac.fitnesse.fixture.slim.StopTestException;
import nl.hsac.fitnesse.fixture.slim.web.annotation.NoNgWait;
import nl.hsac.fitnesse.fixture.slim.web.annotation.WaitUntil;
import nl.hsac.fitnesse.fixture.util.selenium.NgClientSideScripts;
import nl.hsac.fitnesse.slim.interaction.ReflectionHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Browser Test targeted to test AngularJs apps.
 */
public class NgBrowserTest extends BrowserTest {
    private final static Set<String> METHODS_NO_WAIT;
    private String angularRoot = "[ng-app], [ng_app], [data-ng-app], [x-ng-app], [ng\\:app]";

    static {
        METHODS_NO_WAIT = ReflectionHelper.validateMethodNames(
                NgBrowserTest.class,
                "open",
                "takeScreenshot",
                "location",
                "back",
                "forward",
                "refresh",
                "alertText",
                "confirmAlert",
                "dismissAlert",
                "openInNewTab",
                "ensureActiveTabIsNotClosed",
                "currentTabIndex",
                "tabCount",
                "ensureOnlyOneTab",
                "closeTab",
                "setAngularRoot",
                "switchToNextTab",
                "switchToPreviousTab",
                "switchToDefaultContent",
                "switchToFrame",
                "switchToParentFrame",
                "waitForAngularRequestsToFinish",
                "secondsBeforeTimeout",
                "secondsBeforePageLoadTimeout",
                "waitForPage",
                "waitForTagWithText",
                "waitForClassWithText",
                "waitForClass",
                "waitForVisible",
                "waitSeconds",
                "waitMilliseconds",
                "waitMilliSecondAfterScroll",
                "screenshotBaseDirectory",
                "screenshotShowHeight",
                "setBrowserWidth",
                "setBrowserHeight",
                "setBrowserSizeToBy",
                "setBrowserSizeToMaximum",
                "setGlobalValueTo",
                "isImplicitWaitForAngularEnabled",
                "setImplicitWaitForAngularTo",
                "globalValue",
                "clearSearchContext",
                "setAngularRoot",
                "getAngularRoot");
    }

    /**
     * Creates new.
     */
    public NgBrowserTest() {
        setImplicitWaitForAngularTo(true);
    }

    @Override
    protected void waitForAngularIfNeeded(Method method) {
        if (isImplicitWaitForAngularEnabled()
                && requiresWaitForAngular(method)) {
            waitForAngularRequestsToFinish();
        }
    }

    /**
     * Determines whether method requires waiting for all Angular requests to finish
     * before it is invoked.
     * @param method method to be invoked.
     * @return true, if waiting for Angular is required, false otherwise.
     */
    protected boolean requiresWaitForAngular(Method method) {
        String methodName = method.getName();
        return !METHODS_NO_WAIT.contains(methodName) && !hasNoWaitAnnotation(method);
    }
    
    protected boolean hasNoWaitAnnotation(Method method) {
    	return method.isAnnotationPresent(NoNgWait.class);
    }

    @Override
    public boolean open(String address) {
        boolean result = super.open(address);
        if (result && isImplicitWaitForAngularEnabled()) {
            waitForAngularRequestsToFinish();
        }
        return result;
    }

    public void waitForAngularRequestsToFinish() {
        String root = getAngularRoot();
        if (root == null) {
            root = "body";
        }
        try {
            waitForAngularRequestsToFinish(root);
        } catch (RuntimeException e) {
            handleExceptionWhileWaiting(root, e);
        } finally {
            // store root for future reference
            setAngularRoot(root);
        }
    }

    protected void handleExceptionWhileWaiting(String rootSelector, RuntimeException e) {
        List<WebElement> roots;
        try {
            roots = findAllByCss(rootSelector);
        } catch (RuntimeException ex) {
            System.err.print("Problem using rootSelector: " + rootSelector);
            ex.printStackTrace();
            throw e;
        }

        if (roots.isEmpty()) {
            System.err.println("Unable to locate Angular root element. Please configure it explicitly using setAngularRoot(selector)");
        } else if (roots.size() == 1) {
            System.err.println("Found Angular. Single root element found, but error while waiting for requests to finish.");
        } else {
            System.err.println("Found Angular. Multiple root elements seem to be present: "
                    + roots.size()
                    + " using root selector: " + rootSelector);
        }
        System.err.println("Retrying once");
        waitForAngularRequestsToFinish(rootSelector);
    }

    protected void waitForAngularRequestsToFinish(String root) {
        Object result = waitForJavascriptCallback(NgClientSideScripts.WaitForAngular, root);
        if (result != null) {
            String msg = getSlimFixtureExceptionMessage("angular", result.toString(), null);
            throw new StopTestException(false, msg);
        }
    }

    @Override
    public String valueFor(String place) {
        String result;
        WebElement angularModelBinding = getAngularElement(place);
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
        WebElement angularModelSelect = findSelect(place);
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
        WebElement angularModelInput = getAngularElementToEnterIn(place);
        if (angularModelInput == null) {
            result = super.enterAs(value, place);
        } else {
            angularModelInput.clear();
            sendValue(angularModelInput, value);
            result = true;
        }
        return result;
    }

    public int numberOf(String repeater) {
        return findRepeaterRows(repeater).size();
    }

    @WaitUntil
    public String valueOfColumnNumberInRowNumberOf(int columnIndex, int rowIndex, String repeater) {
        return getTextInRepeaterColumn(Integer.toString(columnIndex), rowIndex, repeater);
    }

    @WaitUntil
    public String valueOfInRowNumberOf(String columnName, int rowIndex, String repeater) {
        String columnIndex = getXPathForColumnIndex(columnName);
        return getTextInRepeaterColumn(columnIndex, rowIndex, repeater);
    }

    @WaitUntil
    public String valueOfInRowWhereIsOf(String requestedColumnName, String selectOnColumn, String selectOnValue, String repeater) {
        String result = null;
        String compareIndex = getXPathForColumnIndex(selectOnColumn);
        List<WebElement> rows = findRepeaterRows(repeater);
        for (WebElement row : rows) {
            String compareValue = getColumnText(row, compareIndex);
            if ((selectOnValue == null && compareValue == null)
                    || selectOnValue != null && selectOnValue.equals(compareValue)) {
                String requestedIndex = getXPathForColumnIndex(requestedColumnName);
                result = getColumnText(row, requestedIndex);
                break;
            }
        }
        return result;
    }

    protected String getTextInRepeaterColumn(String columnIndexXPath, int rowIndex, String repeater) {
        String result = null;
        List<WebElement> rows = findRepeaterRows(repeater);
        if (rows.size() >= rowIndex) {
            WebElement row = rows.get(rowIndex - 1);
            result = getColumnText(row, columnIndexXPath);
        }
        return result;
    }

    private String getColumnText(WebElement row, String columnIndexXPath) {
        By xPath = getSeleniumHelper().byXpath("td[%s]", columnIndexXPath);
        WebElement cell = row.findElement(xPath);
        return getElementText(cell);
    }

    protected WebElement getAngularElementToEnterIn(String place) {
        return findElement(place);
    }

    protected WebElement getAngularElement(String place) {
        WebElement element = findBinding(place);
        if (element == null) {
            element = findElement(place);
        }
        return element;
    }

    protected WebElement findBinding(String place) {
        return findNgElementByJavascript(NgClientSideScripts.FindBindings, place, true, null);
    }

    protected WebElement findSelect(String place) {
        // this script does not need angular root
        return findByJavascript(NgClientSideScripts.FindSelects, place);
    }

    protected WebElement findElement(String place) {
        return findNgElementByJavascript(NgClientSideScripts.FindElements, place, null);
    }

    protected List<WebElement> findRepeaterRows(String repeater) {
        // this script does not need angular root
        return findAllByJavascript(NgClientSideScripts.FindAllRepeaterRows, repeater, true);
    }

    protected List<WebElement> findNgElementsByJavascript(String script, Object... parameters) {
        Object[] arguments = getFindArguments(parameters);
        return findAllByJavascript(script, arguments);
    }

    protected WebElement findNgElementByJavascript(String script, Object... parameters) {
        Object[] arguments = getFindArguments(parameters);
        return findByJavascript(script, arguments);
    }

    private Object[] getFindArguments(Object[] parameters) {
        List<Object> params = new ArrayList<Object>(parameters.length + 1);
        params.addAll(Arrays.asList(parameters));
        params.add(getAngularRoot());
        return params.toArray();
    }

    /**
     * @return CSS selector expression used to find root of application.
     */
    public String getAngularRoot() {
        return angularRoot;
    }

    /**
     * Defines which CSS selector to use to find the application root.
     * @param anAngularRoot CSS selector expression.
     */
    public void setAngularRoot(String anAngularRoot) {
        angularRoot = anAngularRoot;
    }
}
