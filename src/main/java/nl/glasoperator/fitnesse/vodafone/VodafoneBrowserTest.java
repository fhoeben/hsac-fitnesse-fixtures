package nl.glasoperator.fitnesse.vodafone;

import nl.hsac.fitnesse.fixture.slim.web.BrowserTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Test fixture to run Selenium tests against Vodafone sites.
 */
public class VodafoneBrowserTest extends BrowserTest {
    @Override
    public boolean clickImpl(String place) {
        boolean result = clickFirstButton(place);
        if (!result) {
            result = super.clickImpl(place);
        }
        return result;
    }

    public String selectConnectDateWeeksInFuture(int weekCount) throws ParseException {
        String result = null;
        String name = "connect_date";
        WebElement defaultDateElem = findByXPath("//span[@class='default %s']", name);
        if (defaultDateElem != null) {
            if (click(name)) {
                String defaultDate = defaultDateElem.getText();
                Calendar c = getCalendar(defaultDate);
                int currentMonth = c.get(Calendar.MONTH);
                int currentYear = c.get(Calendar.YEAR);
                c.add(Calendar.WEEK_OF_MONTH, weekCount);
                int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
                int targetMonth = c.get(Calendar.MONTH);
                int targetYear = c.get(Calendar.YEAR);
                int yearsToAdvance = targetYear - currentYear;
                int monthToAdvance = targetMonth - currentMonth + (yearsToAdvance * 12);
                WebElement nextElem = findByXPath("//th[@class='next']");
                if (nextElem != null) {
                    for (int i = 0; i < monthToAdvance; i++) {
                        if (!nextElem.isEnabled() || !nextElem.isDisplayed())
                        {
                            throw new RuntimeException("Kan " + c + " niet kiezen");
                        }
                        nextElem.click();
                    }
                    List<WebElement> elements = findAllByXPath("//td[@class = 'day' and text() = '%s']", ""+dayOfMonth);
                    if (elements != null) {
                        for (WebElement element : elements) {
                            if (clickElement(element)) {
                                result = new SimpleDateFormat("dd-MM-yyyy").format(c.getTime());
                                break;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private Calendar getCalendar(String defaultDate) throws ParseException {
        Date date = new SimpleDateFormat("dd-MM-yyyy").parse(defaultDate);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c;
    }

    private boolean clickFirstButton(String place) {
        boolean result = false;
        List<WebElement> elements = findAllByXPath("//button[text() = '%s']", place);
        if (elements != null) {
            for (WebElement element : elements) {
                if (clickElement(element)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public String globalError() {
        String result = null;
        List<WebElement> elements = findAllByXPath("//div[contains(@class, 'alert-formerror')]");
        if (elements != null) {
            for (WebElement element : elements) {
                if (element.isDisplayed()) {
                    result = element.getText();
                    break;
                }
            }
        }
        return result;
    }

    public String errorOn(String label) {
        String result = null;
        WebElement element = findErrorMessageElement(label);
        if (element != null) {
            result = element.getText();
        }
        return result;
    }

    private WebElement findErrorMessageElement(String label) {
        WebElement element = findByXPath("//label[text() = '%s']/following-sibling::div/p[@class='help-block']",
                label);
        if (element == null) {
            element = findByXPath("//label[normalize-space(text()) = '%s']/following-sibling::p[@class='help-block']",
                    label);
            if (element == null) {
                element = findByXPath("//input[@aria-label = '%s']/../../../following-sibling::p[@class='help-block']",
                                label);
                if (element == null) {
                    element = findByXPath("//label[normalize-space(text()) = '%s']/../following-sibling::p[@class='help-block']",
                                    label);
                    if (element == null) {
                        element = findByXPath("//h3[normalize-space(text()) = '%s']/..//div[contains (@class, 'errormessage')]",
                                label);
                    }
                }
            }
        }
        return element;
    }

    public boolean errorStyleOn(String label) {
        boolean result = false;
        WebElement element = findControlGroup(label);
        if (element != null) {
            result = hasErrorClass(element);
        }
        return result;
    }

    private WebElement findControlGroup(String label) {
        WebElement element = findByXPath("//label[normalize-space(text()) = '%s']/ancestor::div[contains(@class, 'control-group')]", label);
        if (element == null) {
            element = findByXPath("//input[@aria-label = '%s']/ancestor::div[contains(@class, 'control-group')]",
                    label);
        }
        return element;
    }

    private boolean hasErrorClass(WebElement element) {
        boolean result = false;
        String classAttr = element.getAttribute("class");
        if (classAttr != null) {
            String[] classes = classAttr.split(" ");
            result = Arrays.asList(classes).contains("error");
        }
        return result;
    }

    public String errorsOnOthersThan(String label) {
        return getNonEmptyText("//label[normalize-space(text()) != '%s']/following-sibling::div/p[@class='help-block' and normalize-space(text()) != '']",
                                label);
    }

    public String errorStyleOnOthersThan(String label) {
        return getNonEmptyText("//div[contains(@class, 'error') and label[normalize-space(text()) != '%s']]/label",
                                label);
    }

    public int stepCount() {
        int result = 0;
        List<WebElement> elements = findAllByXPath("//div[@class = 'progressbar_wrapper']/ul/li");
        if (elements != null) {
            result = elements.size();
        }
        return result;
    }

    private String getNonEmptyText(String xpathExpr, String... params) {
        String result = null;
        List<WebElement> elements = findAllByXPath(xpathExpr, params);
        if (elements != null) {
            List<String> texts = new ArrayList<String>(elements.size());
            for (WebElement element : elements) {
                String labelText = element.getText();
                if (!"".equals(labelText)) {
                    texts.add(labelText);
                }
            }
            if (!texts.isEmpty()) {
                result = texts.toString();
            }
        }
        return result;
    }

}
