package nl.hsac.fitnesse.fixture.slim.web;

import nl.hsac.fitnesse.fixture.slim.web.annotation.WaitUntil;
import nl.hsac.fitnesse.fixture.util.selenium.NgClientSideScripts;
import nl.hsac.fitnesse.fixture.util.selenium.by.GridBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Browser Test targeted to test AngularJs apps.
 */
public class NgBrowserTest extends NgBrowserTestBase {
    /**
     * Creates new.
     */
    public NgBrowserTest() {
        super(NgClientSideScripts.WaitForAngular);
        setAngularRoot("[ng-app], [ng_app], [data-ng-app], [x-ng-app], [ng\\:app]");
    }

    public NgBrowserTest(int secondsBeforeTimeout) {
        super(secondsBeforeTimeout, NgClientSideScripts.WaitForAngular);
        setAngularRoot("[ng-app], [ng_app], [data-ng-app], [x-ng-app], [ng\\:app]");
    }

    public void waitForAngularRequestsToFinish() {
        String root = getAngularRoot();
        if (root == null) {
            setAngularRoot("body");
        }
        super.waitForAngularRequestsToFinish();
    }

    @Override
    public String valueFor(String place) {
        String result;
        WebElement element = findByTechnicalSelectorOr(place, this::getAngularElement);
        if (element == null) {
            result = super.valueFor(place);
        } else {
            result = valueFor(element);
        }
        return result;
    }

    @Override
    public boolean selectFor(String value, String place) {
        boolean result;
        WebElement element = findByTechnicalSelectorOr(place, this::findSelect);
        if (element == null) {
            result = super.selectFor(value, place);
        } else {
            result = clickSelectOption(element, value);
        }
        return result;
    }

    @Override
    public boolean enterAs(String value, String place) {
        boolean result;
        WebElement input = findByTechnicalSelectorOr(place, this::getAngularElementToEnterIn);
        if (input == null) {
            result = super.enterAs(value, place);
        } else {
            result = enter(input, value, true);
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

    /**
     * Creates an XPath expression that will determine, for a row, which index to use to select the cell in the column
     * with the supplied header text value.
     * @param columnName name of column in header (th)
     * @return XPath expression which can be used to select a td in a row
     */
    protected String getXPathForColumnIndex(String columnName) {
        return GridBy.getXPathForColumnIndex(columnName);
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
}
