package org.example.generator.annotation;

import java.lang.annotation.*;

@Inherited
@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Generatable {
    Class<?>[] implementsFor() default {};
}
