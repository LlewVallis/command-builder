package io.github.llewvallis.commandbuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Infers an argument parser by invoking the zero argument constructor of a class.
 */
@ParserAnnotation(value = ArgumentInferers.class, factoryMethodName = "createParserFromClassArg")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface ClassArg {

    Class<?> value();
}
