package io.github.llewvallis.commandbuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an annotation as being able to generate an {@link ArgumentParser} during argument inference.
 *
 * When inferring the argument, a static factory method will be called and its return value will be used as the argument
 * parser. The factory method must take {@link ArgumentInferenceContext} as its sole argument, unless
 * {@link #transformsPrevious()} is true, in which case it must take an additional {@link ArgumentParser} argument.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface ParserAnnotation {

    /**
     * The class which contains the factory method.
     */
    Class<?> value();

    /**
     * The name of the factory method.
     */
    String factoryMethodName() default "createParserFromAnnotation";

    /**
     * Whether this annotation applies a transformation to an inferred parser rather than generate a parser itself.
     */
    boolean transformsPrevious() default false;
}
