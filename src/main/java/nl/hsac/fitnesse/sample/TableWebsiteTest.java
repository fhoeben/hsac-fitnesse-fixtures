package nl.hsac.fitnesse.sample;

import nl.hsac.fitnesse.fixture.slim.web.BrowserTest;
import org.openqa.selenium.WebElement;

/**
 * Sample {@link BrowserTest} subclasses which defines a custom heuristic to find elements.
 */
public class TableWebsiteTest extends BrowserTest<WebElement> {
    @Override
    protected WebElement getElement(String place, String container) {
        return findFirstInContainer(container, place,
                () -> findByXPath("(.//td[descendant-or-self::text()[normalized(.)='%s']])[last()]/following-sibling::td[1]", place),
                // use null for container since this supplier will already be executed in 'container' context
                () -> super.getElement(place, null));
    }
}
