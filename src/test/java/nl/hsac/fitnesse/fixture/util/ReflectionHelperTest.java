package nl.hsac.fitnesse.fixture.util;

import nl.hsac.fitnesse.fixture.Environment;
import nl.hsac.fitnesse.fixture.slim.XmlHttpTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class ReflectionHelperTest {
    private final ReflectionHelper helper = new ReflectionHelper();

    @Test
    public void testCanSetAndGetPrivateFieldOnEnvironment() {
        HtmlCleaner myCleaner = new HtmlCleaner();
        Environment env = Environment.getInstance();

        helper.setField(env, "htmlCleaner", myCleaner);

        assertSame(myCleaner, env.getHtmlCleaner());

        assertSame(myCleaner, helper.getField(env, "htmlCleaner"));
    }

    @Test
    public void testCanSetAndGetPrivateFieldOfSuperClass() {
        String newValue = "Hallo";
        XmlHttpTest xmlHttpTest = new XmlHttpTest();
        String ct = xmlHttpTest.getContentType();
        assertNotEquals(newValue, ct);

        helper.setField(xmlHttpTest, "contentType", newValue);

        assertEquals(newValue, xmlHttpTest.getContentType());
        assertEquals(newValue, helper.getField(xmlHttpTest, "contentType"));
    }

    @Test
    public void testOwnMethod() throws NoSuchMethodException {
        Deprecated anno = helper.getAnnotation(Deprecated.class, AnnotatedClass.class.getMethod("notAnnotated", String.class));
        assertNull(anno);

        anno = helper.getAnnotation(Deprecated.class, AnnotatedClass.class.getMethod("annotated", String.class));
        assertNotNull(anno);
    }

    @Test
    public void testSuperMethod() throws NoSuchMethodException {
        Deprecated anno = helper.getAnnotation(Deprecated.class, AnnotatedSubClass.class.getMethod("notAnnotated", String.class));
        assertNull(anno);

        anno = helper.getAnnotation(Deprecated.class, AnnotatedSubClass.class.getMethod("annotated", String.class));
        assertNotNull(anno);
    }

    @Test
    public void testInterfaceMethod() throws NoSuchMethodException {
        Deprecated anno = helper.getAnnotation(Deprecated.class, AnnotatedSubClass.class.getMethod("nonAnnotatedInterfaceMethod", ReflectionHelperTest.class));
        assertNull(anno);

        anno = helper.getAnnotation(Deprecated.class, AnnotatedSubClass.class.getMethod("annotatedInterfaceMethod", ReflectionHelperTest.class));
        assertNotNull(anno);
    }

    @Test
    public void testCached() throws NoSuchMethodException {
        Deprecated anno = helper.getAnnotation(Deprecated.class, AnnotatedSubClass.class.getMethod("nonAnnotatedInterfaceMethod", ReflectionHelperTest.class));
        assertNull(anno);
        anno = helper.getAnnotation(Deprecated.class, AnnotatedSubClass.class.getMethod("nonAnnotatedInterfaceMethod", ReflectionHelperTest.class));
        assertNull(anno);

        anno = helper.getAnnotation(Deprecated.class, AnnotatedSubClass.class.getMethod("annotatedInterfaceMethod", ReflectionHelperTest.class));
        assertNotNull(anno);

        anno = helper.getAnnotation(Deprecated.class, AnnotatedSubClass.class.getMethod("annotatedInterfaceMethod", ReflectionHelperTest.class));
        assertNotNull(anno);
    }

    private static class AnnotatedClass {
        public void notAnnotated(String h) {
        }
        @Deprecated
        public Object annotated(String h) {
            return null;
        }
    }

    private interface AnnotatedInterface {
        @Deprecated
        void annotatedInterfaceMethod(ReflectionHelperTest test);
        void nonAnnotatedInterfaceMethod(ReflectionHelperTest test);
    }

    private static class AnnotatedSubClass extends AnnotatedClass implements AnnotatedInterface {
        @Override
        public void notAnnotated(String h) {
        }
        @Override
        public String annotated(String h) {
            return null;
        }

        @Override
        public void annotatedInterfaceMethod(ReflectionHelperTest test) {
        }

        @Override
        public void nonAnnotatedInterfaceMethod(ReflectionHelperTest test) {
        }
    }

}
