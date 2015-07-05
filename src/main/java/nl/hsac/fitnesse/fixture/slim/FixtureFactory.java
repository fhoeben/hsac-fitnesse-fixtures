package nl.hsac.fitnesse.fixture.slim;

import fitnesse.slim.fixtureInteraction.DefaultInteraction;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Factory to create fixture instances that behave like the way they do when Slim invokes them.
 * This means #aroundSlimInvoke() is applied around their normal methods, so that they can be used by
 * 'normal' Java classes and not just the wiki.
 */
public class FixtureFactory {
    public <T extends SlimFixture> T create(Class<T> clazz) {
        return create(clazz, null, null);
    }

    public <T extends SlimFixture> T create(Class<T> clazz, int constructorArg) {
        return create(clazz, new Class<?>[] {int.class}, new Object[] {constructorArg});
    }

    public <T extends SlimFixture> T create(Class<T> clazz, long constructorArg) {
        return create(clazz, new Class<?>[] {long.class}, new Object[] {constructorArg});
    }

    public <T extends SlimFixture> T create(Class<T> clazz, double constructorArg) {
        return create(clazz, new Class<?>[] {double.class}, new Object[] {constructorArg});
    }

    public <T extends SlimFixture> T create(Class<T> clazz, boolean constructorArg) {
        return create(clazz, new Class<?>[] {boolean.class}, new Object[] {constructorArg});
    }

    public <T extends SlimFixture> T create(Class<T> clazz, Object... constructorArgs) {
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

    public <T extends SlimFixture> T create(Class<T> clazz, Class<?>[] constructorTypes, Object[] constructorArgs) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(new LikeSlimInteraction());
        T result;
        if (constructorArgs != null && constructorArgs.length > 0) {
            result = (T) enhancer.create(constructorTypes, constructorArgs);
        } else {
            result = (T) enhancer.create();
        }
        return result;
    }

    private static class LikeSlimInteraction implements MethodInterceptor {
        private static final DefaultInteraction INTERACTION = new DefaultInteraction();
        private boolean aroundInvoked = false;

        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            if (!aroundInvoked
                    && Modifier.isPublic(method.getModifiers())
                    && !method.getDeclaringClass().equals(Object.class)
                    && !"aroundSlimInvoke".equals(method.getName())) {
                aroundInvoked = true;
                try {
                    return ((SlimFixture) obj).aroundSlimInvoke(INTERACTION, method, args);
                } finally {
                    aroundInvoked = false;
                }
            } else {
                return proxy.invokeSuper(obj, args);
            }
        }
    }

}
