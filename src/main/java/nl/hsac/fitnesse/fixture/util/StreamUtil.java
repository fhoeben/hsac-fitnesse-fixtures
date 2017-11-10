package nl.hsac.fitnesse.fixture.util;

import java.util.LinkedHashMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Helper to add some custom methods dealing with Streams.
 */
public class StreamUtil {
    private StreamUtil() {
        // no instances expected
    }

    /**
     * Collects a stream to a LinkedHashMap, each stream element is expected to produce map entry.
     * @param keyMapper   function to deal with keys.
     * @param valueMapper function to deal with values.
     * @param <T>         type of element in input stream.
     * @param <K>         type of key for created map.
     * @param <U>         type of value for created map.
     * @return map with a key/value for each item in the stream.
     * @throws IllegalArgumentException if keyMapper produces the same key multiple times.
     */
    public static <T, K, U> Collector<T, ?, LinkedHashMap<K, U>> toLinkedMap(Function<? super T, ? extends K> keyMapper,
                                                                             Function<? super T, ? extends U> valueMapper) {
        BinaryOperator<U> mergeFunction = throwingMerger();
        return toLinkedMap(keyMapper, valueMapper, mergeFunction);
    }

    /**
     * Collects a stream to a LinkedHashMap.
     * @param keyMapper   function to deal with keys.
     * @param valueMapper function to deal with values.
     * @param mergeFunction function to apply one values if keyMapper produces same key multiple times.
     * @param <T>         type of element in input stream.
     * @param <K>         type of key for created map.
     * @param <U>         type of value for created map.
     * @return map with a key/value for each item in the stream.
     */
    public static <T, K, U> Collector<T, ?, LinkedHashMap<K, U>> toLinkedMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends U> valueMapper,
            BinaryOperator<U> mergeFunction) {
        return Collectors.toMap(keyMapper, valueMapper, mergeFunction, LinkedHashMap::new);
    }

    /**
     * Throws is same key is produced.
     * @param <T> type of values.
     * @throws IllegalArgumentException always
     */
    public static <T> BinaryOperator<T> throwingMerger() {
        return (u, v) -> {
            throw new IllegalArgumentException(String.format("Duplicate key: value %s was already present now %s is added", u, v));
        };
    }

    /**
     * Returns last/newest value.
     * @param <T> type of values.
     * @return newest value.
     */
    public static <T> BinaryOperator<T> lastOneWinsMerger() {
        return (u, v) -> v;
    }
}
