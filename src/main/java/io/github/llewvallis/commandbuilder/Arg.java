package io.github.llewvallis.commandbuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides metadata for argument inference.
 *
 * Exactly one of {@link #value()} and {@link #member()} must be present.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Arg {

    /**
     * Calls the zero argument constructor of the class to initialize the {@link ArgumentParser}.
     *
     * Although the type of this property is {@code Class<?>} so it can be omitted using {@code void.class}, it requires
     * a {@code Class<? extends ArgumentParser>}.
     */
    Class<?> value() default void.class;

    /**
     * Fetches the argument parser from a method or field of the instance.
     *
     * In the presence of ambiguity, the following rules are used to determine which member is accessed:
     *
     * <ol>
     *     <li>Any private method of the instance's class is used.</li>
     *     <li>Any private field of the instance's class is used.</li>
     *     <li>Any inherited method of the instance's class is used.</li>
     *     <li>Any inherited field of the instance's class is used.</li>
     *     <li>The search is repeated for the parent class, starting a 1.</li>
     * </ol>
     */
    String member() default "";

    /**
     * If true, the parser will be transformed as if by the {@link ArgumentParser#optional()} method.
     */
    boolean optional() default false;
}
