package org.example.classes;

import org.example.generator.annotation.Generatable;

@Generatable
public interface Shape {
    double getArea();
    double getPerimeter();
}