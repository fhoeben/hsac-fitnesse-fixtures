package nl.hsac.fitnesse.fixture.util;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Helper to find first non-null elements using suppliers or functions, limiting the number of executions.
 */
public class FirstNonNullHelper {
    /**
     * Gets first supplier's result which is not null.
     * Suppliers are called sequentially. Once a non-null result is obtained the remaining suppliers are not called.
     *
     * @param <T>       type of result returned by suppliers.
     * @param suppliers all possible suppliers that might be able to supply a value.
     * @return first result obtained which was not null, OR <code>null</code> if all suppliers returned <code>null</code>.
     */
    @SafeVarargs
    public static <T> T firstNonNull(Supplier<T>... suppliers) {
        return firstNonNull(Supplier::get, suppliers);
    }

    /**
     * Gets first result of function which is not null.
     *
     * @param <T>      type of values.
     * @param <R>      element to return.
     * @param function function to apply to each value.
     * @param values   all possible values to apply function to.
     * @return first result which was not null,
     * OR <code>null</code> if either result for all values was <code>null</code> or values was <code>null</code>.
     */
    public static <T, R> R firstNonNull(Function<T, R> function, T[] values) {
        return values == null ? null : firstNonNull(function, Stream.of(values));
    }

    /**
     * Gets first result of function which is not null.
     *
     * @param <T>      type of values.
     * @param <R>      element to return.
     * @param function function to apply to each value.
     * @param values   all possible values to apply function to.
     * @return first result which was not null,
     * OR <code>null</code> if either result for all values was <code>null</code> or values was <code>null</code>.
     */
    public static <T, R> R firstNonNull(Function<T, R> function, Collection<T> values) {
        return values == null ? null : firstNonNull(function, values.stream());
    }

    /**
     * Gets first result of function which is not null.
     *
     * @param <T>       type of values.
     * @param <R>       element to return.
     * @param function  function to apply to each value.
     * @param suppliers all possible suppliers that might be able to supply a value.
     * @return first result which was not null,
     * OR <code>null</code> if result for all supplier's values was <code>null</code>.
     */
    public static <T, R> R firstNonNull(Function<T, R> function, Supplier<Collection<T>>... suppliers) {
        Stream<Supplier<R>> resultStream = Stream.of(suppliers)
                                                    .map(s -> (() -> firstNonNull(function, s.get())));
        return firstNonNull(Supplier::get, resultStream);
    }

    /**
     * Gets first result of set of function which is not null.
     *
     * @param <T>       type of values.
     * @param <R>       element to return.
     * @param input     input to provide to all functions.
     * @param function  first function to apply (separate to indicate at least one should be provided)
     * @param functions all possible functions that might be able to supply a value.
     * @return first result which was not null,
     * OR <code>null</code> if result for each function's results was <code>null</code>.
     */
    public static <T, R> R firstNonNull(T input, Function<T, R> function, Function<T, R>... functions) {
        return firstNonNull(f -> f.apply(input), Stream.concat(Stream.of(function), Stream.of(functions)));
    }

    /**
     * Gets first element which is not null.
     *
     * @param <T>      type of values.
     * @param <R>      element to return.
     * @param function function to apply to each value.
     * @param values   all possible values.
     * @return first result value which was not null, OR <code>null</code> if all values were <code>null</code>.
     */
    public static <T, R> R firstNonNull(Function<T, R> function, Stream<T> values) {
        return values
                .map(function)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
}
