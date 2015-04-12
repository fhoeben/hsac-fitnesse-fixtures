package nl.hsac.fitnesse.fixture.slim.web;

import nl.hsac.fitnesse.fixture.util.NgClientSideScripts;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        waitForAngularRequestsToFinish();
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
        waitForAngularRequestsToFinish();
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

    @Override
    public boolean clear(String place) {
        waitForAngularRequestsToFinish();
        return super.clear(place);
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

    @Override
    public boolean rowExistsWhereIs(String selectOnColumn, String selectOnValue) {
        waitForAngularRequestsToFinish();
        return super.rowExistsWhereIs(selectOnColumn, selectOnValue);
    }

    @Override
    protected boolean clickInRow(String columnXPath, String place) {
        waitForAngularRequestsToFinish();
        return super.clickInRow(columnXPath, place);
    }

    @Override
    public boolean enterAsInRowWhereIs(String value, String requestedColumnName, String selectOnColumn, String selectOnValue) {
        waitForAngularRequestsToFinish();
        return super.enterAsInRowWhereIs(value, requestedColumnName, selectOnColumn, selectOnValue);
    }

    @Override
    protected String getTextByXPath(String xpathPattern, String... params) {
        waitForAngularRequestsToFinish();
        return super.getTextByXPath(xpathPattern, params);
    }

    @Override
    protected String getTextByClassName(String className) {
        waitForAngularRequestsToFinish();
        return super.getTextByClassName(className);
    }

    @Override
    protected String downloadFromRow(String columnXPath, String place) {
        waitForAngularRequestsToFinish();
        return super.downloadFromRow(columnXPath, place);
    }

    @Override
    public String download(String place) {
        waitForAngularRequestsToFinish();
        return super.download(place);
    }

    @Override
    public boolean selectFileFor(String fileName, String place) {
        waitForAngularRequestsToFinish();
        return super.selectFileFor(fileName, place);
    }

    @Override
    protected Alert getAlert() {
        waitForAngularRequestsToFinish();
        return super.getAlert();
    }

    protected String getTextInRepeaterColumn(String columnIndexXPath, int rowIndex, String repeater) {
        String result = null;
        waitForAngularRequestsToFinish();
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
