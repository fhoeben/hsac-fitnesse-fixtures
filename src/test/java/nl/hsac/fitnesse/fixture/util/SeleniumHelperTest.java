package nl.hsac.fitnesse.fixture.util;

import nl.hsac.fitnesse.fixture.util.selenium.SeleniumHelper;
import org.junit.Test;
import org.openqa.selenium.By;

import static org.junit.Assert.assertEquals;

public class SeleniumHelperTest {
    private final SeleniumHelper helper = new SeleniumHelper();

    @Test
    public void testFindByXPathNormal() {
        By by = helper.byXpath(".//option[normalize-space(translate(text(), '\u00a0', ' ')) = '%s']", "A");
        assertEquals("By.xpath: .//option[normalize-space(translate(text(), '\u00a0', ' ')) = 'A']", by.toString());
    }

    @Test
    public void testFindByXPathNormalizedExact() {
        By by = helper.byXpath(".//option[normalized(text()) = '%s']", "A");
        assertEquals("By.xpath: .//option[normalize-space(translate(text(), '\u00a0', ' ')) = 'A']", by.toString());

        by = helper.byXpath(".//option[normalized(.) = '%s']", "A");
        assertEquals("By.xpath: .//option[normalize-space(translate(., '\u00a0', ' ')) = 'A']", by.toString());

        by = helper.byXpath(".//option[normalized(descendant::text()) = '%s']", "A");
        assertEquals("By.xpath: .//option[normalize-space(translate(descendant::text(), '\u00a0', ' ')) = 'A']",
                        by.toString());

        by = helper.byXpath(".//option[normalized(descendant::text()) = '%s']" +
                            "//option[normalized(descendant::text()) = '%s']", "A", "B");
        assertEquals("By.xpath: .//option[normalize-space(translate(descendant::text(), '\u00a0', ' ')) = 'A']" +
                            "//option[normalize-space(translate(descendant::text(), '\u00a0', ' ')) = 'B']",
                        by.toString());
    }

    @Test
    public void testFindByXPathNormalizedInParams() {
        By by = helper.byXpath(".//option[%s = '%s']", "normalized(text())", "A");
        assertEquals("By.xpath: .//option[normalize-space(translate(text(), '\u00a0', ' ')) = \"A\"]", by.toString());

        by = helper.byXpath(".//option[%s = '%s']", "normalized(.)", "A");
        assertEquals("By.xpath: .//option[normalize-space(translate(., '\u00a0', ' ')) = \"A\"]", by.toString());

        by = helper.byXpath(".//option[%s = '%s']", "normalized(descendant::text())", "A");
        assertEquals("By.xpath: .//option[normalize-space(translate(descendant::text(), '\u00a0', ' ')) = \"A\"]",
                by.toString());

        by = helper.byXpath(".//option[%s = '%s']" +
                "//option[%s = '%s']", "normalized(descendant::text())", "A", "normalized(descendant::text())", "B");
        assertEquals("By.xpath: .//option[normalize-space(translate(descendant::text(), '\u00a0', ' ')) = \"A\"]" +
                        "//option[normalize-space(translate(descendant::text(), '\u00a0', ' ')) = \"B\"]",
                by.toString());
    }

    @Test
    public void testFindByXPathNormalizedContains() {
        By by = helper.byXpath(".//option[contains(normalized(text()), '%s')]", "A");
        assertEquals("By.xpath: .//option[contains(normalize-space(translate(text(), '\u00a0', ' ')), 'A')]",
                        by.toString());

        by = helper.byXpath(".//option[contains(normalized(.), '%s')]", "A");
        assertEquals("By.xpath: .//option[contains(normalize-space(translate(., '\u00a0', ' ')), 'A')]", by.toString());

        by = helper.byXpath(".//option[contains(normalized(descendant::text()), '%s')]", "A");
        assertEquals("By.xpath: .//option[contains(normalize-space(translate(descendant::text(), '\u00a0', ' ')), 'A')]",
                        by.toString());

        by = helper.byXpath(".//option[contains(normalized(descendant::text()), '%s')]" +
                        "//option[contains(normalized(descendant::text()), '%s')]", "A", "B");
        assertEquals("By.xpath: .//option[contains(normalize-space(translate(descendant::text(), '\u00a0', ' ')), 'A')]" +
                            "//option[contains(normalize-space(translate(descendant::text(), '\u00a0', ' ')), 'B')]",
                        by.toString());
    }
}
