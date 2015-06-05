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
                "<div><span>Hello </span><ins class=\"collapse_rim\">&nbsp;&nbsp;&nbsp;&nbsp;</ins><span>sir </span></div>",
                fixture.differenceBetweenAnd("Hello sir ", "Hello     sir "));
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

    @Test
    public void testShowDiffWithNewline() {
        assertEquals("Same string", "<div>1 \n 3</div>", fixture.differenceBetweenAnd("1 \n 3", "1 \n 3"));
        assertEquals("Different strings",
                "<div><span>Hello \n</span><ins class=\"collapse_rim\">&nbsp;<br/></ins><span>sir</span><ins class=\"collapse_rim\">\t</ins><span>\nBye\r\nSee You tomorrow</span></div>",
                fixture.differenceBetweenAnd("Hello \nsir\nBye\r\nSee You tomorrow", "Hello \n\nsir\t\nBye\r\nSee You tomorrow"));
    }

    @Test
    public void testShowDiffWithExplicitWhitespace() {
        assertEquals("Same string", "<div>1 \n 3</div>", fixture.differenceBetweenExplicitWhitespaceAnd("1 \n 3", "1 \n 3"));
        assertEquals("Different strings",
                "<div><span>Hello \n" +
                        "</span><ins class=\"collapse_rim\">&para;<br/></ins><span>sir</span><ins class=\"collapse_rim\">&rarr;</ins><span>\n" +
                        "Bye</span><del class=\"collapse_rim\">&#8629;</del><span>\n" +
                        "See</span><del class=\"collapse_rim\">&bull;</del><ins class=\"collapse_rim\">&middot;</ins><span>You tomorrow</span></div>",
                fixture.differenceBetweenExplicitWhitespaceAnd("Hello \nsir\nBye\r\nSee\u00A0You tomorrow", "Hello \n\nsir\t\nBye\nSee You tomorrow"));
    }

    @Test
    public void testShowDiffIgnoreWhitespace() {
        assertEquals("Same string", "<div>1 \n 3</div>", fixture.differenceBetweenIgnoreWhitespaceAnd("1 \n 3", "1 \n 3"));
        assertEquals("Same string with &nbsp;", "<div>1 \n 3</div>", fixture.differenceBetweenIgnoreWhitespaceAnd("1 \n 3", "1 \u00A0 \n\u00A0 3"));
        assertEquals("Different strings only different in whitespace",
                "<div>Hello \n" +
                        "sir\n" +
                        "Bye\r\n" +
                        "See You tomorrow</div>",
                fixture.differenceBetweenIgnoreWhitespaceAnd("Hello \nsir\nBye\r\nSee You tomorrow", "Hello \n\nsir\t\nBye\r\nSee You tomorrow"));
        assertEquals("Different string 2nd shorter",
                "<div><span>Hello</span><del class=\"collapse_rim\">&nbsp;sir</del></div>",
                fixture.differenceBetweenIgnoreWhitespaceAnd("Hello sir", "Hello"));

        assertEquals("Both null", null, fixture.differenceBetweenIgnoreWhitespaceAnd(null, null));
        assertEquals("One null", "<div><ins class=\"collapse_rim\">Hello</ins></div>", fixture.differenceBetweenIgnoreWhitespaceAnd(null, "Hello"));
    }

    @Test
    public void testShowDifferencesPreformat() {
        assertEquals("Same string", "<pre>Hello dear madam   Bye</pre>", fixture.differenceBetweenIgnoreWhitespaceAnd("<pre>Hello dear madam   Bye</pre>", "<pre>Hello dear madam Bye</pre>"));
        assertEquals("Different string", "<pre><span>Hello dear madam </span><del class=\"collapse_rim\">&lt;p&gt;Bye&lt;/p&gt;</del><ins class=\"collapse_rim\">Bye</ins></pre>",
                        fixture.differenceBetweenIgnoreWhitespaceAnd("<pre>Hello dear madam <p>Bye</p></pre>", "<pre>Hello dear madam Bye</pre>"));
    }
}
