package nl.hsac.fitnesse.fixture.util.selenium;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.openqa.selenium.Cookie;

import java.util.Set;

/**
 * Converts Selenium cookies.
 */
public class CookieConverter {
    /**
     * Converts Selenium cookies to Apache http client ones.
     * @param browserCookies cookies in Selenium format.
     * @param cookieStore store to place coverted cookies in.
     */
    public void copySeleniumCookies(Set<Cookie> browserCookies, CookieStore cookieStore) {
        for (Cookie browserCookie : browserCookies) {
            ClientCookie cookie = convertCookie(browserCookie);
            cookieStore.addCookie(cookie);
        }
    }

    /**
     * Converts Selenium cookie to Apache http client.
     * @param browserCookie selenium cookie.
     * @return http client format.
     */
    protected ClientCookie convertCookie(Cookie browserCookie) {
        BasicClientCookie cookie = new BasicClientCookie(browserCookie.getName(), browserCookie.getValue());
        String domain = browserCookie.getDomain();
        if (domain != null && domain.startsWith(".")) {
            // http client does not like domains starting with '.', it always removes it when it receives them
            domain = domain.substring(1);
        }
        cookie.setDomain(domain);
        cookie.setPath(browserCookie.getPath());
        cookie.setExpiryDate(browserCookie.getExpiry());
        cookie.setSecure(browserCookie.isSecure());
        if (browserCookie.isHttpOnly()) {
            cookie.setAttribute("httponly", "");
        }
        return cookie;
    }
}
