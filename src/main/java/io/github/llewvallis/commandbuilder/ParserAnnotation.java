package io.github.llewvallis.commandbuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an annotation as being able to generate an {@link ArgumentParser} during argument inference.
 *
 * Any annotation marked with this one can be used in place of {@link Arg}. When inferring the argument, a static
 * factory method with the marked annotation as its only parameter will be called and its return value will be used as
 * the argument parser.
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
     *
     * The factory method must:
     * <ol>
     *     <li>Be static.</li>
     *     <li>Return an instance of {@link ArgumentParser}.</li>
     *     <li>Take an instance of the marked annotation as its only argument.</li>
     * </ol>
     */
    String factoryMethodName() default "createParserFromAnnotation";
}
