package org.example.generator;

import org.example.common.RandomProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class PrimitiveValueGenerator {

    private static final Map<Class<?>, Supplier<?>> DEFAULTS = new HashMap<>();

    static {
        DEFAULTS.put(int.class, () -> RandomProvider.RANDOM.nextInt(100));
        DEFAULTS.put(Integer.class, () -> RandomProvider.RANDOM.nextInt(100));
        DEFAULTS.put(long.class, RandomProvider.RANDOM::nextLong);
        DEFAULTS.put(Long.class, RandomProvider.RANDOM::nextLong);
        DEFAULTS.put(double.class, RandomProvider.RANDOM::nextDouble);
        DEFAULTS.put(Double.class, RandomProvider.RANDOM::nextDouble);
        DEFAULTS.put(float.class, RandomProvider.RANDOM::nextFloat);
        DEFAULTS.put(Float.class, RandomProvider.RANDOM::nextFloat);
        DEFAULTS.put(boolean.class, RandomProvider.RANDOM::nextBoolean);
        DEFAULTS.put(Boolean.class, RandomProvider.RANDOM::nextBoolean);
        DEFAULTS.put(char.class, () -> (char) (RandomProvider.RANDOM.nextInt(26) + 'a'));
        DEFAULTS.put(Character.class, () -> (char) (RandomProvider.RANDOM.nextInt(26) + 'a'));
        DEFAULTS.put(byte.class, () -> (byte) RandomProvider.RANDOM.nextInt());
        DEFAULTS.put(Byte.class, () -> (byte) RandomProvider.RANDOM.nextInt());
        DEFAULTS.put(short.class, () -> (short) RandomProvider.RANDOM.nextInt(Short.MAX_VALUE + 1));
        DEFAULTS.put(Short.class, () -> (short) RandomProvider.RANDOM.nextInt(Short.MAX_VALUE + 1));
        DEFAULTS.put(String.class, () -> UUID.randomUUID().toString().substring(0, 8));
    }

    static Optional<Supplier<?>> getGenerator(Class<?> type) {
        return Optional.ofNullable(DEFAULTS.get(type));
    }
}
