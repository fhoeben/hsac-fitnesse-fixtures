package nl.glasoperator.fitnesse.vodafone;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

import java.util.List;

/**
 * Fixture to test pick and choose and product detail pages for Vodafone.
 */
public class VodafoneChooseBrowserTest extends VodafoneBrowserTest {
    private static final String CART_PRODUCT_ITEM_PATTERN = "//div[@id = 'overview_order']//li[contains(normalize-space(div/b/text()), '%s')]/div[@class = 'order-right']";

    public boolean pickPackage(final String type, final String packageName) {
        boolean result = false;
        WebElement element = findByXPath("//h3[contains(normalize-space(text()), '%s')]/..//h3[text() = '%s']/..", type, packageName);
        if (element != null) {
            final String currentPackage = chosenPackageFor(type);
            result = clickElement(element);
            if (result) {
                // wait for element to be (un)highlighted
                result = waitUntil(new ExpectedCondition<Boolean>() {
                    @Override
                    public Boolean apply(WebDriver webDriver) {
                        boolean isChosen = packageName.equals(chosenPackageFor(type));
                        boolean result;
                        if (packageName.equals(currentPackage)) {
                            // package clicked was already chosen, it must be unselected
                            result = !isChosen;
                        } else {
                            // package clicked was not yet chosen, it must be selected
                            result = isChosen;
                        }
                        if (result && "Internet".equals(type)) {
                            waitMilliSeconds(250);
                        }
                        return result;
                    }
                });
                if (result
                        && "Televisie".equals(type)
                        && "Extra".equals(packageName)
                        && !packageName.equals(currentPackage)) {
                    result = waitForTagWithText("h3", "Welke 2 extra zenderpakketten wil je erbij?");
                }
            }
        }
        return result;
    }

    public boolean enabledPackage(String type, String packageName) {
        boolean result = false;
        boolean retry = true;
        for (int i = 0;
             !result && retry;
             i++) {
            try {
                result = pickPackage(type, packageName);
            } catch (WebDriverException e) {
                String msg = e.getMessage();
                if (!msg.contains("Other element would receive the click")) {
                    retry = false;
                } else {
                    if (i < 1) {
                        waitSeconds(1);
                    } else {
                        retry = false;
                    }
                }
            }
        }
        return result;
    }

    public String chosenPackageFor(String type) {
        String groupName = type.toLowerCase();
        if ("bellen".equals(groupName)) {
            groupName = "telefonie";
        }
        WebElement group = findByXPath("//div[@class = 'maingroup %s group']", groupName);
        if (group != null) {
            scrollTo(group);
        }
        return getTextByXPath("//h3[contains(normalize-space(text()), '%s')]/..//div[contains(@class, ' selected')]//h3", type);
    }

    public String cartTotal(String categoryName) {
        return getTextByXPath("//div[@id = 'overview_costs']//li[contains(normalize-space(text()), '%s')]/div/span",
                                categoryName);
    }

    public String cartProductsPrice(String productName) {
        return getTextByXPath(CART_PRODUCT_ITEM_PATTERN + "/span", productName);
    }

    public String cartProductsPeriod(String productName) {
        String result = getTextByXPath(CART_PRODUCT_ITEM_PATTERN, productName);
        if (result != null && result.contains("\n")) {
            result = result.split("\\n")[1];
        } else {
            result = null;
        }
        return result;
    }

    //click the option to add a extra service
    public boolean clickOptionButton(String type, String choice) {
        return clickByXPath("//h3[normalize-space(text()) = '%s']/..//a[normalize-space(text()) = '%s']/..//label",
                                type, choice);
    }

    //The selector of predefined packages
    public boolean clickIn(String buttonText, String packageName) {
        return clickByXPath("//h2[contains(normalize-space(text()), '%s')]/..//a[text() = '%s']",
                                packageName, buttonText);
    }

    public boolean clickTvPackage(String packageName) {
        return clickByXPath("//span[contains(@class, 'label')]/b[contains(normalize-space(text()), '%s')]",
                packageName);
    }

    private boolean clickByXPath(String xpathExpr, String... params) {
        boolean result = false;
        WebElement element = findByXPath(xpathExpr, params);
        if (element != null) {
            result = clickElement(element);
        }
        return result;
    }

    //check if messagebox appears
    public boolean messageBox() {
        boolean result = false;
        List<WebElement> elements = findAllByXPath("//div[@id = 'messagebox' and contains(@class, 'message red')]");
        if (elements != null) {
            for (WebElement element : elements) {
                if (element.isDisplayed()) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    //check if messagebox appears
    public String messageBoxText() {
        return getTextByXPath("//div[@id = 'messagebox']/p[2]");
    }

    public boolean rejectPickPackage(final String type, final String packageName) {
        boolean result = false;
        WebElement element = findByXPath("//h3[contains(normalize-space(text()), '%s')]/..//h3[text() = '%s']/..", type, packageName);
        if (null != element) {
            result = false;
            }
        else {
            result = clickElement(element);
        }
        return result;
    }

    public Boolean productNotInCart(String productName) {
        boolean result = true;
        String product = getTextByXPath(CART_PRODUCT_ITEM_PATTERN + "/span", productName);
        if (product != null) {
            return result = false;
        }
        return result;
    }

    public String addressInMessagebox() {
        String result = null;
        WebElement element = findByXPath("//b[@class='address']");
        if (element != null) {
            scrollIfNotDisplayed(element);
            result = element.getText();
        }
        return result;
    }

    public boolean clickOption(String type, String choice) {
        return clickByXPath("//div[@class='accordion-heading']/a[normalize-space(text()) = '%s']/..//label[normalize-space(text()) = '%s']",
                type, choice);
    }



}
