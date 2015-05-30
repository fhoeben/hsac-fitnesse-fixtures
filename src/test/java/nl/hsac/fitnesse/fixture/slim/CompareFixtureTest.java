package nl.hsac.fitnesse.fixture.slim;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CompareFixtureTest {
    private final CompareFixture fixture = new CompareFixture();

    @Test
    public void testShowDiff() {
        assertEquals("Same string", "<div>Hello</div>", fixture.differenceBetweenAnd("Hello", "Hello"));
        assertEquals("Different string 2nd longer",
                "<div><span>Hello</span><ins class=\"collapse_rim\">&nbsp;sir</ins></div>",
                fixture.differenceBetweenAnd("Hello", "Hello sir"));
        assertEquals("Different string 2nd shorter",
                "<div><span>Hello</span><del class=\"collapse_rim\">&nbsp;sir</del></div>",
                fixture.differenceBetweenAnd("Hello sir", "Hello"));
        assertEquals("Different string insert",
                "<div><span>Hello </span><ins class=\"collapse_rim\">dear&nbsp;</ins><span>sir</span></div>",
                fixture.differenceBetweenAnd("Hello sir", "Hello dear sir"));
        assertEquals("Different string delete",
                "<div><span>Hello </span><del class=\"collapse_rim\">dear&nbsp;</del><span>sir</span></div>",
                fixture.differenceBetweenAnd("Hello dear sir", "Hello sir"));
        assertEquals("Different string replace",
                "<div><span>Hello </span><del class=\"collapse_rim\">dear</del><ins class=\"collapse_rim\">stupid</ins><span> sir</span></div>",
                fixture.differenceBetweenAnd("Hello dear sir", "Hello stupid sir"));
        assertEquals("Different string extra spaces",
                "<div><span>Hello </span><ins class=\"collapse_rim\">&nbsp;</ins><span>sir</span></div>",
                fixture.differenceBetweenAnd("Hello sir", "Hello  sir"));
        assertEquals("Different string extra spaces",
                "<div><span>Hello </span><ins class=\"collapse_rim\">&nbsp;  &nbsp;</ins><span>sir</span></div>",
                fixture.differenceBetweenAnd("Hello sir", "Hello     sir"));
    }

    @Test
    public void testShowDiffWithNull() {
        assertEquals("Both null", null, fixture.differenceBetweenAnd(null, null));
        assertEquals("One null", "<div><ins class=\"collapse_rim\">Hello</ins></div>", fixture.differenceBetweenAnd(null, "Hello"));
        assertEquals("Two null", "<div><del class=\"collapse_rim\">Bye</del></div>", fixture.differenceBetweenAnd("Bye", null));
    }

    @Test
    public void testShowDiffWithHtmlChars() {
        assertEquals("Same string", "<div>1 &lt; 3</div>", fixture.differenceBetweenAnd("1 < 3", "1 < 3"));
        assertEquals("Different strings",
                "<div><span>Hello </span><del class=\"collapse_rim\">&gt;</del><ins class=\"collapse_rim\">&lt;</ins></div>",
                fixture.differenceBetweenAnd("Hello >", "Hello <"));
    }
}
