package nl.hsac.fitnesse.fixture.web;

import nl.hsac.fitnesse.fixture.util.SeleniumHelper;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BrowserTest {
    private static final Pattern PATTERN = Pattern.compile("<a href=\"(.*?)\">(.*?)</a>", Pattern.CASE_INSENSITIVE);

    private List<List<String>> commands = new ArrayList<List<String>>();
    private SeleniumHelper seleniumHelper = new SeleniumHelper();

    public boolean open(String htmlLink) {
        String url = urlFromLink(htmlLink);
        commands.add(Arrays.asList("open", url));
        SeleniumHelper.getWebDriver().navigate().to(url);
        return true;
    }

    public boolean enterAs(String value, String place) {
        return enterFor(value, place);
    }

    public boolean enterFor(String value, String place) {
        boolean result = false;
        commands.add(Arrays.asList("enter", place, value));
        WebElement element = getElement(place);
        if (element != null) {
            element.sendKeys(value);
            result = true;
        }
        return result;
    }

    public boolean selectAs(String value, String place) {
        return selectFor(value, place);
    }

    public boolean selectFor(String value, String place) {
        commands.add(Arrays.asList("select", place, value));
        return true;
    }

    public boolean click(String place) {
        boolean result = false;
        commands.add(Arrays.asList("click", place));
        WebElement element = getElement(place);
        if (element != null) {
            element.click();
            result = true;
        }
        return result;
    }

    public boolean valueOfIs(String place, String expectedValue) {
        return valueForIs(place, expectedValue);
    }

    public boolean valueForIs(String place, String expectedValue) {
        commands.add(Arrays.asList("valueForIs", place, expectedValue));
        return true;
    }

    public String valueOf(String place) {
        return valueFor(place);
    }

    public String valueFor(String place) {
        commands.add(Arrays.asList("valueFor", place));
        return "Bob.";
    }

    public boolean clear(String place) {
        boolean result = false;
        commands.add(Arrays.asList("clear", place));
        WebElement element = getElement(place);
        if (element != null) {
            element.clear();
            result = true;
        }
        return result;
    }

    private WebElement getElement(String place) {
        return seleniumHelper.getElement(place);
    }

    public List<List<String>> commands() {
        return commands;
    }

    private String urlFromLink(String htmlLink) {
        String result = null;
        Matcher matcher = PATTERN.matcher(htmlLink);
        if (matcher.matches()) {
            result = matcher.group(1);
        }
        return result;
    }
}
