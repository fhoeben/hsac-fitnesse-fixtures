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
     * @param cookieVersion version to use for cookies (Selenium does not provide version information), -1 means set no
     *                      version.
     */
    public void copySeleniumCookies(Set<Cookie> browserCookies, CookieStore cookieStore, int cookieVersion) {
        for (Cookie browserCookie : browserCookies) {
            ClientCookie cookie = convertCookie(browserCookie, cookieVersion);
            cookieStore.addCookie(cookie);
        }
    }

    /**
     * Converts Selenium cookie to Apache http client.
     * @param browserCookie selenium cookie.
     * @param version version of cookie to create (use -1 not to explicitly set value)
     * @return http client format.
     */
    protected ClientCookie convertCookie(Cookie browserCookie, int version) {
        BasicClientCookie cookie = new BasicClientCookie(browserCookie.getName(), browserCookie.getValue());
        if (version > -1) {
            cookie.setVersion(version);
        }
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
