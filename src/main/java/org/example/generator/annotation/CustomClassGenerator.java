package org.example.generator.annotation;

import java.lang.annotation.*;

@Inherited
@Target(value= ElementType.TYPE)
@Retention(value= RetentionPolicy.RUNTIME)
public @interface CustomClassGenerator {
    Class<?>[] implementsFor() default {};
}
