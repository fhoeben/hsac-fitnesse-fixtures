package nl.hsac.fitnesse.slim.interaction;

import fitnesse.slim.fixtureInteraction.DefaultInteraction;
import nl.hsac.fitnesse.fixture.slim.SlimFixture;
import nl.hsac.fitnesse.fixture.util.HtmlCleaner;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class FixtureFactoryTest {
    private static final List<Class<?>> classes = new ArrayList<Class<?>>();

    private FixtureFactory fixtureFactory;
    private TrackingInteraction trackingInteraction;

    @Before
    public void setUp() throws Exception {
        trackingInteraction = new TrackingInteraction();
        fixtureFactory = new FixtureFactory();
        fixtureFactory.setInteraction(trackingInteraction);
    }

    @AfterClass
    public static void afterAll() {
        assertFalse(classes.isEmpty());
        assertFalse(classes.size() == 1);
        Class<?> firstClazz = null;
        for (Class<?> clazz : classes) {
            if (firstClazz == null) {
                firstClazz = clazz;
            }
            assertSame("Multiple subclasses generated", firstClazz, clazz);
        }
    }

    @Test
    public void testAroundInvokeUsed() {
        MyTestFixture test = fixtureFactory.create(MyTestFixture.class);
        classes.add(test.getClass());

        assertEquals("Hello 10", test.sayHello());
        assertEquals("Hello John", test.sayHelloTo("John"));
        assertEquals("<pre>&lt;xml&gt;Hello Pete&lt;/xml&gt;</pre>", test.sayHelloToInXml("Pete").replaceAll("\\r?\\n", ""));

        List<Method> methods = trackingInteraction.getCalledMethods();
        // InteractionAwareFixture should route our calls to the FixtureInteraction
        assertEquals("Unexpected number of calls via the FixtureInteraction", 3, methods.size());
    }

    @Test
    public void testAroundInvokeUsedWithConstructorArg() {
        MyTestFixture test = fixtureFactory.create(MyTestFixture.class, new Class<?>[] {int.class}, new Object[] {15});
        classes.add(test.getClass());

        assertEquals("Hello 15", test.sayHello());
        assertEquals("Hello John", test.sayHelloTo("John"));
        assertEquals("<pre>&lt;xml&gt;Hello Pete&lt;/xml&gt;</pre>", test.sayHelloToInXml("Pete").replaceAll("\\r?\\n", ""));

        List<Method> methods = trackingInteraction.getCalledMethods();
        // InteractionAwareFixture should route our calls to the FixtureInteraction
        assertEquals("Unexpected number of calls via the FixtureInteraction", 3, methods.size());
    }

    @Test
    public void testAroundInvokeUsedWithConstructorArgShort() {
        MyTestFixture test = fixtureFactory.create(MyTestFixture.class, 17);
        classes.add(test.getClass());

        assertEquals("Hello 17", test.sayHello());
        assertTrue(test.waitMilliseconds(2));
        assertEquals("Hello John", test.sayHelloTo("John"));
        assertEquals("<pre>&lt;xml&gt;Hello Pete&lt;/xml&gt;</pre>", test.sayHelloToInXml("Pete").replaceAll("\\r?\\n", ""));

        List<Method> methods = trackingInteraction.getCalledMethods();
        // InteractionAwareFixture should route our calls to the FixtureInteraction
        assertEquals("Unexpected number of calls via the FixtureInteraction", 4, methods.size());
    }

    public static class MyTestFixture extends SlimFixture {
        private final HtmlCleaner cleaner = getEnvironment().getHtmlCleaner();
        private final int myI;

        public MyTestFixture() {
            this(10);
        }

        public MyTestFixture(int i) {
            myI = i;
        }

        public String sayHello() {
            return "Hello " + myI;
        }

        public String sayHelloTo(String name) {
            return "Hello " + cleaner.cleanupValue(name);
        }

        public String sayHelloToInXml(String name) {
            return getEnvironment().getHtmlForXml("<xml>Hello " + name + "</xml>");
        }
    }

    private static class TrackingInteraction extends DefaultInteraction {
        private final List<Method> calledMethods = new ArrayList<Method>();

        @Override
        public Object methodInvoke(Method method, Object instance, Object... convertedArgs) throws Throwable {
            calledMethods.add(method);
            return super.methodInvoke(method, instance, convertedArgs);
        }

        public List<Method> getCalledMethods() {
            return calledMethods;
        }
    }
}
