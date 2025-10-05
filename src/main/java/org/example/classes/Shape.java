package org.example.classes;

import org.example.generator.annotation.CustomClassGenerator;

@CustomClassGenerator
public interface Shape {
    double getArea();
    double getPerimeter();
}