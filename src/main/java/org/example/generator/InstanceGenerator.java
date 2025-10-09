package org.example.generator;

import org.example.common.RandomProvider;
import org.example.generator.annotation.Generatable;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InstanceGenerator {

    private final ImplementationIndex implementationIndex;
    private final ThreadLocal<Deque<Class<?>>> constructing = ThreadLocal.withInitial(ArrayDeque::new);

    public InstanceGenerator() {
        var discovered = ClassPathScanner.scanForAnnotatedClasses();
        this.implementationIndex = new ImplementationIndex(discovered);
    }

    public Object generateValueOf(Class<?> type) {
        return generateValueOf(type, -1);
    }

    private Object generateValueOf(Class<?> type, int depth) {
        var primitive = PrimitiveValueGenerator.getGenerator(type);
        if (primitive.isPresent()) {
            return primitive.get().get();
        }

        if (type.isInterface()) {
            var impl = implementationIndex.randomImplementationFor(type);
            if (impl.isEmpty())
                throw new IllegalArgumentException("Не найдены реализации интерфейса: " + type.getName());
            return generateSafely(impl.get(), depth);
        }

        if (Modifier.isAbstract(type.getModifiers())) {
            throw new IllegalArgumentException("Класс " + type.getName() + " является абстрактным, не удаётся создать экземпляр");
        }

        if (depth == 0) {
            return null;
        }

        var stack = constructing.get();
        if (stack.contains(type) && !Objects.equals(stack.peek(), type)) {
            return null;
        }

        stack.push(type);
        try {
            if (!type.isAnnotationPresent(Generatable.class)) {
                throw new IllegalArgumentException("Класс " + type.getName() + " нельзя сгенерировать. Требуется аннотация @CustomClassGenerator");
            }

            var constructors = type.getDeclaredConstructors();
            if (constructors.length == 0) throw new IllegalArgumentException("Класс " + type.getName() + " не имеет конструкторов");

            Constructor<?> constructor = constructors[RandomProvider.RANDOM.nextInt(constructors.length)];

            int allowedForChildren = depth > 0 ? depth - 1 : -1;
            var args = buildArgsForConstructor(constructor, allowedForChildren);
            try {
                return type.cast(constructor.newInstance(args));
            } catch (Throwable e) {
                throw new RuntimeException("Не удалось создать экземпляр " + type.getName(), e);
            }
        } finally {
            stack.pop();
            if (stack.isEmpty()) constructing.remove();
        }
    }

    private Object generateSafely(Class<?> currentClass, int depth) {
        try {
            return generateValueOf(currentClass, depth);
        } catch (Exception e) {
            throw new RuntimeException("Не удается создать значение для " + currentClass.getName(), e);
        }
    }

    private Object generateSafely(Class<?> currentClass) {
        return generateSafely(currentClass, -1);
    }

    private Object[] buildArgsForConstructor(Constructor<?> constructor, int allowedSelfDepthForChildren) {
        var parameters = constructor.getParameters();
        var args = new Object[parameters.length];
        Class<?> declaringClass = constructor.getDeclaringClass();

        final Integer[] perConstructorSelfDepth = new Integer[] {
                allowedSelfDepthForChildren >= 0 ? allowedSelfDepthForChildren : null
        };

        IntStream.range(0, parameters.length).forEach(i -> {
            var parameter = parameters[i];
            var parameterParameterizedType = parameter.getParameterizedType();
            var parameterType = parameter.getType();

            if (parameterParameterizedType instanceof ParameterizedType parameterizedType) {
                var rawType = parameterizedType.getRawType();

                if (rawType instanceof Class<?> rawClass && Collection.class.isAssignableFrom(rawClass)) {
                    var actualTypeArguments = parameterizedType.getActualTypeArguments();

                    if (actualTypeArguments.length == 1) {
                        var elementClass = resolveClassFromType(actualTypeArguments[0]);
                        if (elementClass == null) {
                            args[i] = Collections.emptyList();
                            return;
                        }

                        var size = 1 + RandomProvider.RANDOM.nextInt(3);
                        var created = IntStream.range(0, size)
                                .mapToObj(j -> generateSafely(elementClass))
                                .collect(Collectors.toList());
                        args[i] = created;
                        return;
                    }

                    args[i] = Collections.emptyList();
                    return;
                }
            }

            if (parameterType.isArray()) {
                var componentType = parameterType.getComponentType();
                var len = 1 + RandomProvider.RANDOM.nextInt(3);
                var array = Array.newInstance(componentType, len);

                IntStream.range(0, len)
                        .forEach(j -> Array.set(array, j, generateSafely(componentType)));
                args[i] = array;
                return;
            }

            if (Collection.class.isAssignableFrom(parameterType)) {
                args[i] = Collections.emptyList();
                return;
            }

            if (parameterType.isInterface()) {
                var impl = implementationIndex.randomImplementationFor(parameterType);
                args[i] = impl.map(this::generateSafely).orElse(null);
                return;
            }

            if (parameterType.equals(declaringClass)) {
                if (perConstructorSelfDepth[0] == null) {
                    perConstructorSelfDepth[0] = 1 + RandomProvider.RANDOM.nextInt(3);
                }
                int depthToPass = perConstructorSelfDepth[0];
                args[i] = generateSafely(parameterType, depthToPass);
                return;
            }

            args[i] = generateSafely(parameterType);
        });

        return args;
    }

    private Class<?> resolveClassFromType(Type type) {
        if (type instanceof Class<?>) return (Class<?>) type;

        if (type instanceof ParameterizedType) {
            Type raw = ((ParameterizedType) type).getRawType();
            if (raw instanceof Class<?>) return (Class<?>) raw;
        }

        if (type instanceof GenericArrayType) {
            var genericComponentType = ((GenericArrayType) type).getGenericComponentType();
            var classFromType = resolveClassFromType(genericComponentType);
            if (classFromType != null) return Array.newInstance(classFromType, 0).getClass();
        }

        return null;
    }
}