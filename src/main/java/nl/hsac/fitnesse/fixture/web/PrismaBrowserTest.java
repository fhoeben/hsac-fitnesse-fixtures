package nl.hsac.fitnesse.fixture.web;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Script fixture for testing prisma web application.
 */
public class PrismaBrowserTest extends BrowserTest {
    static final int MAX_WAIT_SECONDS = 30;
    public PrismaBrowserTest() {
        System.setProperty("webdriver.ie.driver", "C:\\dev\\IEDriverServer_Win32_2.33.0\\IEDriverServer.exe");
    }

    @Override
    public String pageTitle() {
        String pageTitle;
        String fullPageTitle = super.pageTitle();
        if (fullPageTitle.contains(" - ")) {
            pageTitle = fullPageTitle.substring(0, fullPageTitle.indexOf(" - "));
        } else {
            pageTitle = fullPageTitle;
        }
        return pageTitle;
    }

    public boolean clickAndWaitForPage(String place, final String pageTitleStart) {
        boolean result = click(place);
        if (result) {
            result = waitFor(
                        new ExpectedCondition<Boolean>() {
                            @Override
                            public Boolean apply(WebDriver wd) {
                                return wd.getTitle().startsWith(pageTitleStart);
                            }
                         });
        }
        return result;
    }

    public boolean enterDateFor(String value, String place) {
        boolean result = false;
        String[] values = value.split("-");
        if (values.length == 3) {
            WebElement element = getElement(place);
            if ("span".equalsIgnoreCase(element.getTagName())) {
                String id = element.getAttribute("id");

                String dayId = id + "_d";
                result = enterForId(values[0], dayId);
                String monthId = id + "_m";
                result = enterForId(values[1], monthId);
                String yearId = id + "_y";
                result = enterForId(values[2], yearId);

                String hiddenId = id + "_h";
                result = enterForHidden(value.replace("-", "."), hiddenId);
            }
        }
        return result;
    }

    private boolean enterForId(String value, String id) {
        boolean result = false;
        WebElement element = getSeleniumHelper().findElement(true, By.id(id));
        if (element != null) {
            element.sendKeys(value);
            result = true;
        }
        return result;
    }

    public boolean waitForToBe(String place, final String value) {
        boolean result = false;
        final WebElement element = getElement(place);
        if (element != null) {
            final String id = element.getAttribute("id");
            String currentText = element.getText();
            result = value.equalsIgnoreCase(currentText);
            if (!result) {
                result = waitFor(
                            new ExpectedCondition<Boolean>() {
                                @Override
                                public Boolean apply(WebDriver wd) {
                                    WebElement elem = wd.findElement(By.id(id));
                                    String text = elem.getText();
                                    return value.equalsIgnoreCase(text);
                                }
                            });

            }
        }
        return result;
    }

    private Boolean waitFor(ExpectedCondition<Boolean> condition) {
        return waitDriver().until(condition).booleanValue();
    }

    private WebDriverWait waitDriver() {
        return new WebDriverWait(getSeleniumHelper().driver(), MAX_WAIT_SECONDS);
    }

}
