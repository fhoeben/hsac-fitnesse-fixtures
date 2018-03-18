package nl.hsac.fitnesse.fixture.util.selenium;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PageSourceSaverTest {
    private PageSourceSaver saver = new PageSourceSaver("", null);
    @Test
    public void testGetPathAndQuery() {
        assertEquals("/mypath/index", saver.getPathAndQuery("http://localhost/mypath/index"));
        assertEquals("/mypath/index?1521378256368", saver.getPathAndQuery("http://localhost:8080/mypath/index?1521378256368"));
        assertEquals("/mypath/index?c=1521378256368&a=b", saver.getPathAndQuery("http://localhost:8080/mypath/index?c=1521378256368&a=b"));
    }
}
