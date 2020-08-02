package io.github.llewvallis.commandbuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Transforms the previous argument inferrer by settings it {@link ArgumentParser#isOptional()} value to the value of
 * {@link #optional()}.
 */
@ParserAnnotation(value = ArgumentInferers.class, factoryMethodName = "createParserFromOptionalArg", transformsPrevious = true)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface OptionalArg {

    /**
     * Whether to force the argument to be optional or non-optional.
     */
    boolean optional() default true;
}
