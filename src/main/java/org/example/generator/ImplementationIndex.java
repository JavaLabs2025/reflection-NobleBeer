package org.example.generator;

import org.example.common.RandomProvider;
import org.example.generator.annotation.Generatable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class ImplementationIndex {

    private final Set<Class<?>> annotatedTypes;
    private final Map<Class<?>, List<Class<?>>> interfaceToImplementations = new HashMap<>();

    public ImplementationIndex(Set<Class<?>> annotatedTypes) {
        this.annotatedTypes = Objects.requireNonNull(annotatedTypes);
        buildIndex();
    }

    public Optional<Class<?>> randomImplementationFor(Class<?> currentInterface) {
        return Optional.ofNullable(interfaceToImplementations.get(currentInterface))
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(RandomProvider.RANDOM.nextInt(list.size())));
    }

    private void buildIndex() {
        annotatedTypes.stream()
                .flatMap(currentClass -> {
                    Generatable annotation = currentClass.getAnnotation(Generatable.class);

                    if (annotation != null && annotation.implementsFor().length > 0) {
                        return Arrays.stream(annotation.implementsFor())
                                .map(target -> Map.entry(target, currentClass));
                    }

                    return Arrays.stream(currentClass.getInterfaces())
                            .map(currentInterface -> Map.entry(currentInterface, currentClass));
                })
                .forEach(entry -> register(entry.getKey(), entry.getValue()));
    }

    private void register(Class<?> currentInterface, Class<?> implementedClass) {
        interfaceToImplementations
                .computeIfAbsent(currentInterface, k -> new ArrayList<>())
                .add(implementedClass);
    }
}
