package it.cs.contact.tracing.utils;

import java.util.AbstractMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import it.cs.contact.tracing.audio.DecibelMeter;

/**
 * Map Utils
 *
 * @author claudio.sini
 */
public class MapUtils {

    private MapUtils() {
    }

    public static <K, V> Map.Entry<K, V> entry(final K key, final V value) {

        return new AbstractMap.SimpleEntry<>(key, value);
    }

    public static <K, U> Collector<Map.Entry<K, U>, ?, Map<K, U>> entriesToMap() {
        return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    public static NavigableMap<Integer, DecibelMeter.Noise> toNavigableMap(final Map<Integer, DecibelMeter.Noise> collect) {
        return new TreeMap<>(collect);
    }
}
