package nl.hsac.fitnesse.fixture.slim;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class JsonFixtureTest {
    private JsonFixture fixture = new JsonFixture();

    @Test
    public void testElementOfJsonPathNoResult() {
        ArrayList<String> emptyList = new ArrayList<>();
        JsonFixture fixtureSpy = Mockito.spy(fixture);
        Mockito.doReturn(emptyList).when(fixtureSpy).listJsonPathMatches("test");
        Object result = fixtureSpy.elementOfJsonPath(0, "test");
        assertNull(result);
    }

    @Test
    public void testElementOfJsonPathResults() {
        ArrayList<String> emptyList = new ArrayList<>();
        emptyList.add("hello");
        JsonFixture fixtureSpy = Mockito.spy(fixture);
        Mockito.doReturn(emptyList).when(fixtureSpy).listJsonPathMatches("test");
        Object result = fixtureSpy.elementOfJsonPath(0, "test");
        assertEquals("hello", result);
    }
}
