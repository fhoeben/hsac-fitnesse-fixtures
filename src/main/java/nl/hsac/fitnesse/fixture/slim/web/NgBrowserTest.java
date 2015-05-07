package nl.hsac.fitnesse.fixture.slim.web;

import nl.hsac.fitnesse.fixture.slim.StopTestException;
import nl.hsac.fitnesse.fixture.util.NgClientSideScripts;
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
    private String angularRoot = null;

    static {
        METHODS_NO_WAIT = ReflectionHelper.validateMethodNames(
                NgBrowserTest.class,
                "open",
                "takeScreenshot",
                "openInNewTab",
                "ensureActiveTabIsNotClosed",
                "currentTabIndex",
                "tabCount",
                "ensureOnlyOneTab",
                "closeTab",
                "setAngularRoot",
                "switchToNextTab",
                "switchToPreviousTab",
                "waitForAngularRequestsToFinish",
                "secondsBeforeTimeout",
                "waitForPage",
                "waitForTagWithText",
                "waitForClassWithText",
                "waitForClass",
                "waitSeconds",
                "waitMilliseconds",
                "waitMilliSecondAfterScroll",
                "screenshotBaseDirectory",
                "screenshotShowHeight",
                "setBrowserWidth",
                "setBrowserHeight",
                "setBrowserSizeToBy",
                "setGlobalValueTo",
                "globalValue",
                "setAngularRoot",
                "getAngularRoot");
    }

    @Override
    protected void waitForAngularIfNeeded(Method method) {
        if (requiresWaitForAngular(method)) {
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
        return !METHODS_NO_WAIT.contains(methodName);
    }

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
        waitForAngularRequestsToFinish();
        return findRepeaterRows(repeater).size();
    }

    public String valueOfColumnNumberInRowNumberOf(int columnIndex, int rowIndex, String repeater) {
        return getTextInRepeaterColumn(Integer.toString(columnIndex), rowIndex, repeater);
    }

    public String valueOfInRowNumberOf(String columnName, int rowIndex, String repeater) {
        String columnIndex = getXPathForColumnIndex(columnName);
        return getTextInRepeaterColumn(columnIndex, rowIndex, repeater);
    }

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
        return findNgElementByJavascript(NgClientSideScripts.FindSelects, place);
    }

    protected WebElement findElement(String place) {
        return findNgElementByJavascript(NgClientSideScripts.FindElements, place, null);
    }

    protected List<WebElement> findRepeaterRows(String repeater) {
        return findNgElementsByJavascript(NgClientSideScripts.FindAllRepeaterRows, repeater, true);
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

    public String getAngularRoot() {
        return angularRoot;
    }

    public void setAngularRoot(String anAngularRoot) {
        angularRoot = anAngularRoot;
    }
}
