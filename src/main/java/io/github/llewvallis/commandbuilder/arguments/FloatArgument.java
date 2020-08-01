package io.github.llewvallis.commandbuilder.arguments;

import io.github.llewvallis.commandbuilder.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Set;

/**
 * An argument that matches optionally bounded floats.
 */
@With
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FloatArgument implements ArgumentParser<Float> {

    private float min = -Float.MAX_VALUE;
    private float max = Float.MAX_VALUE;

    @ParserAnnotation(FloatArgument.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    public @interface Arg {

        float min() default -Float.MAX_VALUE;
        float max() default Float.MAX_VALUE;
    }

    @ArgumentInferenceFactory
    private static FloatArgument createParserFromAnnotation(ArgumentInferenceContext<Arg> ctx) {
        return new FloatArgument(ctx.getAnnotation().min(), ctx.getAnnotation().max());
    }

    @Override
    public Float parse(String argument, int position, CommandContext context) throws ArgumentParseException {
        float value;
        try {
            value = Float.parseFloat(argument);
        } catch (NumberFormatException e) {
            throw new ArgumentParseException("not a valid number");
        }

        if (value < min) {
            throw new ArgumentParseException("should be greater or equal to " + min);
        }

        if (value > max) {
            throw new ArgumentParseException("should be lesser or equal to " + max);
        }

        return value;
    }

    @Override
    public Set<String> complete(List<Object> parsedArguments, String currentArgument, int position, CommandContext context) {
        return Set.of();
    }
}
