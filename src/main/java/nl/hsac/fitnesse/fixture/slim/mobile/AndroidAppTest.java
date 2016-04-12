package nl.hsac.fitnesse.fixture.slim.mobile;

import io.appium.java_client.MobileElement;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import nl.hsac.fitnesse.fixture.slim.web.BrowserTest;
import nl.hsac.fitnesse.fixture.slim.web.annotation.TimeoutPolicy;
import nl.hsac.fitnesse.fixture.slim.web.annotation.WaitUntil;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;

import java.util.concurrent.TimeUnit;

/**
 * Android App fixture for use with an android driver. Aimed at effortlessly testing native apps.
 */
public class AndroidAppTest extends BrowserTest {
    private int swipeSpeed = 1000;
    private int waitAfterSwipe = 0;

    public AndroidAppTest() {
        super("X");
        setImplicitWaitForAngularTo(false);
    }

    public void setImplicitWaitInSeconds(long stepDelay) {
        getAndroidDriver().manage().timeouts().implicitlyWait(stepDelay, TimeUnit.SECONDS);
    }

    public void setSwipeSpeedInMilliseconds(int swipeSpeed) {
        this.swipeSpeed = swipeSpeed;
    }

    public void setWaitAfterSwipe(int waitAfterSwipe) {
        this.waitAfterSwipe = waitAfterSwipe;
    }

    @WaitUntil(TimeoutPolicy.RETURN_FALSE)
    public boolean tap(String locator) {
        boolean result = false;
        MobileElement element = getMobileElement(locator);

        if (element != null) {
            TouchAction tap = new TouchAction(getAndroidDriver()).tap(element);
            tap.perform();

            result = true;
        }

        return result;
    }

    @WaitUntil(TimeoutPolicy.RETURN_FALSE)
    public boolean enterAs(String value, String locator) {
        boolean result = false;
        MobileElement element = getMobileElement(locator);

        if (element != null) {
            element.sendKeys(value);
            getAndroidDriver().hideKeyboard();
            result = true;
        }

        return result;
    }

    @WaitUntil(TimeoutPolicy.RETURN_NULL)
    public String valueOf(String locator) {
        MobileElement el = getMobileElement(locator);
        return el.getText();
    }

    @WaitUntil(TimeoutPolicy.RETURN_FALSE)
    public Boolean elementDisplayed(String locator) {
        return (getMobileElement(locator).isDisplayed());
    }

    public void swipeUp() {
        Dimension size = getWindowSize();
        int startX = size.width / 2;
        int startY = (int) (size.height * 0.8);
        int endY = (int) (size.height * 0.2);

        swipe(startX, startY, startX, endY);
    }

    public void swipeDown() {
        Dimension size = getWindowSize();
        int startX = size.width / 2;
        int startY = (int) (size.height * 0.2);
        int endY = (int) (size.height * 0.8);

        swipe(startX, startY, startX, endY);
    }

    public void swipeLeft() {
        Dimension size = getWindowSize();
        int startX = (int) (size.width * 0.8);
        int endX = (int) (size.width * 0.2);
        int startY = size.height / 2;

        swipe(startX, startY, endX, startY);
    }

    public void swipeRight() {
        Dimension size = getWindowSize();
        int startX = (int) (size.width * 0.2);
        int endX = (int) (size.width * 0.8);
        int startY = size.height / 2;

        swipe(startX, startY, endX, startY);
    }

    public boolean swipeFrom(String direction, String locator) {
        boolean result = false;
        MobileElement element = getMobileElement(locator);

        if (element != null) {
            Dimension size = getWindowSize();
            Point elementLocation = element.getLocation();
            int startX = elementLocation.getX();
            int startY = elementLocation.getY();
            int endX;
            int endY;

            switch (direction.toLowerCase().charAt(0)) {
                case 'u':
                    endX = startX;
                    endY = 0;
                    break;
                case 'd':
                    endX = startX;
                    endY = size.getHeight();
                    break;
                case 'l':
                    endX = 0;
                    endY = startY;
                    break;
                case 'r':
                    endX = size.getWidth();
                    endY = startY;
                    break;
                default:
                    System.err.print("ERROR: " + direction + " is not a valid swipe direction.");
                    return false;
            }
            swipe(startX, startY, endX, endY);
            result = true;
        } else {
            System.err.print("ERROR: Element not found for " + locator);
        }
        return result;
    }

    protected void swipe(int startX, int startY, int endX, int endY) {
        swipe(startX, startY, endX, endY, swipeSpeed, waitAfterSwipe);
    }

    protected void swipe(int startX, int startY, int endX, int endY, int speed, int waitAfter) {
        getAndroidDriver().swipe(startX, startY, endX, endY, speed);
        waitMilliseconds(waitAfter);
    }

    public void pressBackButton() {
        getAndroidDriver().navigate().back();
    }

    protected MobileElement getMobileElement(String locator) {
        String lookFor;
        String content;

        //assume text lookup if none is specified (convenient)
        if (!locator.contains("=")) {
            lookFor = "text";
            content = locator;
        } else {
            //Otherwise, use specified UiSelector method
            String[] strategy = locator.split("=");
            lookFor = strategy[0];
            content = strategy[1];
        }

        String using = String.format("new UiSelector().%s(\"%s\")", lookFor, content);
        return getAndroidDriver().findElementByAndroidUIAutomator(using);
    }

    protected Dimension getWindowSize() {
        return getAndroidDriver().manage().window().getSize();
    }

    protected AndroidDriver<MobileElement> getAndroidDriver() {
        return getSeleniumHelper().androidDriver();
    }
}





