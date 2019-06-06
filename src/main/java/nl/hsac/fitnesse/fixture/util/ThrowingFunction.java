package nl.hsac.fitnesse.fixture.util;

import java.util.function.Function;

/**
 * Functional interface to allow exceptions thrown by lambdas to be handled, or wrapped to runtime ones.
 */
@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Exception> {
    R apply(T t) throws E;

    /**
     * Applies using t, wrapping converting any checked exception to a runtime one using exceptionWrapper
     * @param t parameter to supply to {@link #apply(Object)}.
     * @param exceptionWrapper function to convert checked exception to runtime one.
     * @param <RE> type of runtime exception which will be thrown
     * @return result of apply(t)
     */
    default <RE extends RuntimeException> R applyWrapped(T t, Function<E, RE> exceptionWrapper) {
        try {
            return apply(t);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw exceptionWrapper.apply((E) e);
        }
    }
}
