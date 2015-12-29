package nl.hsac.fitnesse.fixture.util;

import fit.exception.FitFailureException;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests NamespaceContextImpl.
 */
public class NamespaceContextImplTest {
    private NamespaceContextImpl impl = new NamespaceContextImpl();

    @Test
    public void testAddGet() {
        String prefix = "p";
        String uri = "aadsad";
        assertNull(impl.getPrefix(uri));
        impl.add(prefix, uri);

        assertEquals(uri, impl.getNamespaceURI(prefix));
        assertEquals(prefix, impl.getPrefix(uri));

        int i = 0;
        Iterator<String> iter = impl.getPrefixes(uri);
        while (iter.hasNext()) {
            i++;
            assertEquals(prefix, iter.next());
        }
        assertEquals(1, i);
    }

    @Test
    public void testAddMultipleGet() {
        String prefix = "p";
        String uri = "aadsad";
        impl.add(prefix, uri);
        String prefix2 = "z";
        impl.add(prefix2, uri);
        String prefix3 = "y";
        String uri3 = "aadsad2121";
        impl.add(prefix3, uri3);

        assertEquals(uri3, impl.getNamespaceURI(prefix3));
        assertEquals(prefix, impl.getPrefix(uri));

        int i = 0;
        Iterator<String> iter = impl.getPrefixes(uri);
        while (iter.hasNext()) {
            i++;
            String prefixValue = iter.next();
            assertTrue(prefixValue.equals(prefix) || prefixValue.equals(prefix2));
        }
        assertEquals(2, i);
    }

    @Test
    public void addDuplicatePrefixOtherUri() {
        String prefix = "p2";
        String uri = "aadsad2";
        String uri2 = "asdad";
        impl.add(prefix, uri);
        
        try {
            impl.add(prefix, uri2);
            fail("expected exception");
        } catch (FitFailureException e) {
            String message = e.getMessage();
            assertTrue(message.contains(uri));
            assertTrue(message.contains(prefix));
        }
    }

    @Test
    public void addDuplicatePrefixSameUri() {
        String prefix = "p2";
        String uri = "aadsad2";
        impl.add(prefix, uri);
        impl.add(prefix, uri);
    }

    @Test
    public void addDuplicatePrefixNullUri() {
        String prefix = "p2";
        String uri = "aadsad2";
        impl.add(prefix, uri);
        impl.add(prefix, null);
        
        assertNull(impl.getNamespaceURI(prefix));
    }

    @Test
    public void addDuplicatePrefixNullUriFirst() {
        String prefix = "p2";
        String uri = "aadsad2";

        impl.add(prefix, null);
        assertNull(impl.getNamespaceURI(prefix));

        impl.add(prefix, uri);
        assertEquals(uri, impl.getNamespaceURI(prefix));
    }
}
