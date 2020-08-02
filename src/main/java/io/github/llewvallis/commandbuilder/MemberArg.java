package io.github.llewvallis.commandbuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Infers an argument parser by looking up the value of either a field or a zero argument method in instance being
 * inferred on.
 */
@ParserAnnotation(value = ArgumentInferers.class, factoryMethodName = "createParserFromMemberArg")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface MemberArg {

    /**
     * The member used to fetch the parser.
     */
    String value();
}
