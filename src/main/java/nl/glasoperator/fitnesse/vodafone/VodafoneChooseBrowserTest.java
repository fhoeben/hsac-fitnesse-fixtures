package nl.glasoperator.fitnesse.vodafone;

import org.openqa.selenium.WebElement;

/**
 * Fixture to test pick and choose and product detail pages for Vodafone.
 */
public class VodafoneChooseBrowserTest extends VodafoneBrowserTest {
    private static final String CART_PRODUCT_ITEM_PATTERN = "//div[@id = 'overview_order']//li[contains(normalize-space(div/b/text()), '%s')]/div[@class = 'order-right']";

    public boolean pickPackage(String type, String packageName) {
        boolean result = false;
        WebElement element = findByXPath("//h3[contains(normalize-space(text()), '%s')]/..//h3[text() = '%s']",
                                            type, packageName);
        if (element != null) {
            result = clickElement(element);
        }
        return result;
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

    public String deliveryAddress() {
        String result = null;
        WebElement element = getSeleniumHelper().getElementByLabelOccurrence("Het adres waar we je aansluiting gaan plaatsen:", 1);
        if (element != null) {
            result = element.getText();
        }
        return result;
    }

    private String getTextByXPath(String xpathPattern, String... params) {
        String result = null;
        WebElement element = findByXPath(xpathPattern, params);
        if (element != null) {
            result = element.getText();
        }
        return result;
    }
}
