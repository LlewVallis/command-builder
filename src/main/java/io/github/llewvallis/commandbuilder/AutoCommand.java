package io.github.llewvallis.commandbuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as eligible for auto command registration.
 *
 * A class annotated with this should inherit from either {@link TopLevelCommand} or {@link CompositeTopLevelCommand}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoCommand {

    /**
     * The factory method used to instantiate the command.
     *
     * If this is an empty string, a zero argument constructor will be used to instantiate the command. Otherwise, this
     * should point to a static method in the annotated class which returns a {@link TopLevelCommand} or
     * {@link CompositeTopLevelCommand} and takes a {@link AutoCommandContext} as its sole parameter.
     */
    String factoryMethod() default "";
}
