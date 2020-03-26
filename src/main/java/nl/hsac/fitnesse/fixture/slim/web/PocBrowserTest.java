package nl.hsac.fitnesse.fixture.slim.web;

import nl.hsac.fitnesse.fixture.slim.web.annotation.TimeoutPolicy;
import nl.hsac.fitnesse.fixture.slim.web.annotation.WaitUntil;
import org.openqa.selenium.WebElement;

import java.util.List;

public class PocBrowserTest extends BrowserTest {
    public PocBrowserTest() {
        super();
    }
    public PocBrowserTest(int secondsBeforeTimeout) {
        super(secondsBeforeTimeout);
    }

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public Integer count(String place) {
        return countIn(place, null);

    }

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public Integer countIn(String place, String container) {
        return (Integer) doInContainer(container, () -> countImpl(place));
    }

    private Integer countImpl(String place) {
        getElement(place);
        List list = findElements(getSeleniumHelper().placeToBy(place));
        return list.size();
    }

}
