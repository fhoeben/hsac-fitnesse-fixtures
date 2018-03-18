package nl.hsac.fitnesse.fixture.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DataUrlHelperTest {

    @Test
    public void isDataUrl() {
        assertTrue(DataUrlHelper.isDataUrl("data:,"));
        assertTrue(DataUrlHelper.isDataUrl("data:text/vnd-example+xyz;foo=bar;base64,R0lGODdh"));
        assertTrue(DataUrlHelper.isDataUrl("data:text/plain;charset=UTF-8;page=21,the%20data:1234,5678"));

        assertFalse(DataUrlHelper.isDataUrl("https:/google.com"));
        assertFalse(DataUrlHelper.isDataUrl("data:text/vnd-example+xyz;foo=bar;base64"));
    }

    @Test
    public void getData() {
        assertEquals("", DataUrlHelper.getData("data:,"));
        assertEquals("R0lGODdh", DataUrlHelper.getData("data:text/vnd-example+xyz;foo=bar;base64,R0lGODdh"));
        assertEquals("the%20data:1234,5678", DataUrlHelper.getData("data:text/plain;charset=UTF-8;page=21,the%20data:1234,5678"));
    }
}