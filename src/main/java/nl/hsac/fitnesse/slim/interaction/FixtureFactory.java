package nl.hsac.fitnesse.slim.interaction;

import fitnesse.slim.fixtureInteraction.DefaultInteraction;
import fitnesse.slim.fixtureInteraction.FixtureInteraction;
import fitnesse.slim.fixtureInteraction.InteractionAwareFixture;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory to create fixture instances that behave like the way they do when Slim invokes them.
 * This means #aroundSlimInvoke() is applied around their normal methods, so that they can be used by
 * 'normal' Java classes and not just the wiki.
 * It is not exactly the same as Slim does, as instance created by this factory do not have #aroundSlimInvoke() applied
 * to calls to 'final' methods. This is a limitation of the technology used.
 */
public class FixtureFactory {
    private static final Map<Class<? extends InteractionAwareFixture>, Factory> FACTORIES = new HashMap<Class<? extends InteractionAwareFixture>, Factory>();
    private FixtureInteraction interaction = null;

    /**
     * @return interaction that will be passed to #aroundSlimInvoke().
     */
    public FixtureInteraction getInteraction() {
        if (interaction == null) {
            interaction = new DefaultInteraction();
        }
        return interaction;
    }

    /**
     * @param interaction interaction to pass to #aroundSlimInvoke().
     */
    public void setInteraction(FixtureInteraction interaction) {
        this.interaction = interaction;
    }

    /**
     * Creates new instance of fixture.
     * @param clazz class to instantiate.
     * @param <T> type to create.
     * @return instance of clazz (subclass, actually) that will have #aroundSlimInvoke() invoked on each method call.
     */
    public <T extends InteractionAwareFixture> T create(Class<T> clazz) {
        return create(clazz, null, null);
    }

    /**
     * Creates new instance of fixture.
     * @param clazz class to instantiate.
     * @param constructorArg argument to pass to constructor of clazz.
     * @param <T> type to create.
     * @return instance of clazz (subclass, actually) that will have #aroundSlimInvoke() invoked on each method call.
     */
    public <T extends InteractionAwareFixture> T create(Class<T> clazz, int constructorArg) {
        return create(clazz, new Class<?>[] {int.class}, new Object[] {constructorArg});
    }

    /**
     * Creates new instance of fixture.
     * @param clazz class to instantiate.
     * @param constructorArg argument to pass to constructor of clazz.
     * @param <T> type to create.
     * @return instance of clazz (subclass, actually) that will have #aroundSlimInvoke() invoked on each method call.
     */
    public <T extends InteractionAwareFixture> T create(Class<T> clazz, long constructorArg) {
        return create(clazz, new Class<?>[] {long.class}, new Object[] {constructorArg});
    }

    /**
     * Creates new instance of fixture.
     * @param clazz class to instantiate.
     * @param constructorArg argument to pass to constructor of clazz.
     * @param <T> type to create.
     * @return instance of clazz (subclass, actually) that will have #aroundSlimInvoke() invoked on each method call.
     */
    public <T extends InteractionAwareFixture> T create(Class<T> clazz, double constructorArg) {
        return create(clazz, new Class<?>[] {double.class}, new Object[] {constructorArg});
    }

    /**
     * Creates new instance of fixture.
     * @param clazz class to instantiate.
     * @param constructorArg argument to pass to constructor of clazz.
     * @param <T> type to create.
     * @return instance of clazz (subclass, actually) that will have #aroundSlimInvoke() invoked on each method call.
     */
    public <T extends InteractionAwareFixture> T create(Class<T> clazz, boolean constructorArg) {
        return create(clazz, new Class<?>[] {boolean.class}, new Object[] {constructorArg});
    }

    /**
     * Creates new instance of fixture.
     * @param clazz class to instantiate.
     * @param constructorArgs arguments to pass to constructor of clazz.
     * @param <T> type to create.
     * @return instance of clazz (subclass, actually) that will have #aroundSlimInvoke() invoked on each method call.
     */
    public <T extends InteractionAwareFixture> T create(Class<T> clazz, Object... constructorArgs) {
        T result;
        if (constructorArgs != null && constructorArgs.length > 0) {
            Class<?>[] types = new Class[constructorArgs.length];
            for (int i = 0; i < constructorArgs.length; i++) {
                types[i] = constructorArgs[i].getClass();
            }
            result = create(clazz, types, constructorArgs);
        } else {
            result = create(clazz, null, null);
        }
        return result;
    }

    /**
     * Creates new instance of fixture.
     * @param clazz class to instantiate.
     * @param constructorTypes types of arguments used to determine which constructor to use.
     * @param constructorArgs arguments to pass to constructor of clazz.
     * @param <T> type to create.
     * @return instance of clazz (subclass, actually) that will have #aroundSlimInvoke() invoked on each method call.
     */
    public <T extends InteractionAwareFixture> T create(Class<T> clazz, Class<?>[] constructorTypes, Object[] constructorArgs) {
        MethodInterceptor callback = createCallback();

        T result;
        if (FACTORIES.containsKey(clazz)) {
            Factory factory = FACTORIES.get(clazz);
            result = createUsingFactory(callback, factory, constructorTypes, constructorArgs);
        } else {
            result = createFirst(callback, clazz, constructorTypes, constructorArgs);
            FACTORIES.put(clazz, (Factory) result);
        }
        return result;
    }

    protected <T extends InteractionAwareFixture> T createUsingFactory(Callback callback, Factory factory, Class<?>[] constructorTypes, Object[] constructorArgs) {
        Callback[] callbacks = new Callback[] { callback };

        T result;
        if (constructorArgs != null && constructorArgs.length > 0) {
            result = (T) factory.newInstance(constructorTypes, constructorArgs, callbacks);
        } else {
            result = (T) factory.newInstance(callbacks);
        }
        return result;
    }

    protected <T extends InteractionAwareFixture> T createFirst(Callback callback, Class<T> clazz, Class<?>[] constructorTypes, Object[] constructorArgs) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(callback);

        T result;
        if (constructorArgs != null && constructorArgs.length > 0) {
            result = (T) enhancer.create(constructorTypes, constructorArgs);
        } else {
            result = (T) enhancer.create();
        }
        return result;
    }

    protected MethodInterceptor createCallback() {
        FixtureInteraction nestedInteraction = getInteraction();
        return new LikeSlimInteraction(nestedInteraction);
    }

    protected static class LikeSlimInteraction implements MethodInterceptor {
        private final FixtureInteraction interaction;
        private boolean aroundInvoked = false;

        public LikeSlimInteraction(FixtureInteraction fixtureInteraction) {
            interaction = fixtureInteraction;
        }

        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            if (!aroundInvoked
                    && Modifier.isPublic(method.getModifiers())
                    && !method.getDeclaringClass().equals(Object.class)
                    && !"aroundSlimInvoke".equals(method.getName())) {
                aroundInvoked = true;
                try {
                    return ((InteractionAwareFixture) obj).aroundSlimInvoke(interaction, method, args);
                } finally {
                    aroundInvoked = false;
                }
            } else {
                return proxy.invokeSuper(obj, args);
            }
        }
    }

}
