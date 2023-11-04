package nl.hsac.fitnesse.fixture.util;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.invoke.MethodType.methodType;

/**
 * Helper to create Functions based on reflection types.
 */
public class LambdaMetaHelper {
    private final static Map<Class<?>, Class<?>> PRIMITIVE_TYPES = new HashMap<>();
    static {
        PRIMITIVE_TYPES.put(boolean.class, Boolean.class);
        PRIMITIVE_TYPES.put(byte.class, Byte.class);
        PRIMITIVE_TYPES.put(short.class, Short.class);
        PRIMITIVE_TYPES.put(char.class, Character.class);
        PRIMITIVE_TYPES.put(int.class, Integer.class);
        PRIMITIVE_TYPES.put(long.class, Long.class);
        PRIMITIVE_TYPES.put(float.class, Float.class);
        PRIMITIVE_TYPES.put(double.class, Double.class);
    }

    /**
     * Gets no-arg constructor as Supplier.
     * @param clazz class to get constructor for.
     * @param <T> clazz.
     * @return supplier.
     */
    public <T> Supplier<T> getConstructor(Class<? extends T> clazz) {
        return getConstructorAs(Supplier.class, "get", clazz);
    }

    /**
     * Gets single arg constructor as Function.
     * @param clazz class to get constructor for.
     * @param arg constructor argument type.
     * @param <T> clazz.
     * @param <A> argument class.
     * @return function.
     */
    public <T, A> Function<A, T> getConstructor(Class<? extends T> clazz, Class<A> arg) {
        return getConstructorAs(Function.class, "apply", clazz, arg);
    }

    /**
     * Gets two arg constructor as BiFunction.
     * @param clazz class to get constructor for.
     * @param arg1 first argument class.
     * @param arg2 second argument class.
     * @param <T> clazz.
     * @param <A1> first argument class
     * @param <A2> second argument class.
     * @return bifunction.
     */
    public <A1, A2, T> BiFunction<A1, A2, T> getConstructor(Class<? extends T> clazz, Class<A1> arg1, Class<A2> arg2) {
        return getConstructorAs(BiFunction.class, "apply", clazz, new Class<?>[] {arg1, arg2});
    }

    protected <T, R> T getConstructorAs(Class<T> targetClass, String methodName, Class<? extends R> clazz, Class<?>... args) {
        try {
            Class<?>[] lambdaArgs = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                Class<?> arg = args[i];
                if (arg.isPrimitive()) {
                    lambdaArgs[i] = PRIMITIVE_TYPES.get(arg);
                } else {
                    lambdaArgs[i] = arg;
                }
            }

            MethodType methodType = methodType(clazz, lambdaArgs);
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle handle = lookup.findConstructor(clazz, methodType(void.class, args));
            MethodType targetType = methodType(targetClass);
            CallSite callSite = LambdaMetafactory.metafactory(lookup, methodName, targetType, methodType.generic(), handle, methodType);
            return (T) callSite.getTarget().invoke();
        } catch (Throwable t) {
            throw new RuntimeException("Unable to create function for constructor of " + clazz.getName(), t);
        }
    }
}
