package org.example.generator;

import org.example.generator.annotation.Generatable;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassPathScanner {

    static Set<Class<?>> scanForAnnotatedClasses() {
        var classpath = System.getProperty("java.class.path");
        if (classpath == null || classpath.isEmpty()) {
            return Collections.emptySet();
        }

        return Arrays.stream(classpath.split(File.pathSeparator))
                .map(File::new)
                .filter(File::exists)
                .filter(File::isDirectory)
                .flatMap(dir -> scanDirectory(dir, "").stream())
                .collect(Collectors.toSet());
    }

    private static Set<Class<?>> scanDirectory(File dir, String targetPackage) {
        var files = dir.listFiles();
        if (files == null) {
            return Collections.emptySet();
        }

        return Arrays.stream(files)
                .flatMap(file -> {
                    if (file.isDirectory()) {
                        var sub = targetPackage.isEmpty() ? file.getName() : targetPackage + "." + file.getName();
                        return scanDirectory(file, sub).stream();
                    } else if (file.getName().endsWith(".class")) {
                        var name = targetPackage + "." + file.getName().substring(0, file.getName().length() - 6);
                        return tryAddClass(name).stream();
                    }
                    return Stream.empty();
                })
                .collect(Collectors.toSet());
    }

    private static Optional<Class<?>> tryAddClass(String className) {
        try {
            var name = Class.forName(className);
            if (name.isAnnotationPresent(Generatable.class)) {
                return Optional.of(name);
            }
        } catch (Throwable ignored) {}

        return Optional.empty();
    }
}
