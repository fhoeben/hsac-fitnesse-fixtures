package nl.hsac.fitnesse.fixture.util;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.invoke.MethodType.methodType;

/**
 * Helper to create Functions based on reflection types.
 */
public class LambdaMetaHelper {

    public <T> Supplier<T> getConstructor(Class<? extends T> clazz) {
        return getConstructorAs(Supplier.class, "get", clazz);
    }

    public <T, A> Function<A, T> getConstructor(Class<? extends T> clazz, Class<A> arg) {
        return getConstructorAs(Function.class, "apply", clazz, arg);
    }

    public <T, U, R> BiFunction<T, U, R> getConstructor(Class<? extends R> clazz, Class<?>... args) {
        return getConstructorAs(BiFunction.class, "apply", clazz, args);
    }

    protected <T, R> T getConstructorAs(Class<T> targetClass, String methodName, Class<? extends R> clazz, Class<?>... args) {
        try {
            MethodType methodType = methodType(clazz, args);
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
