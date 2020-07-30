package io.github.llewvallis.commandbuilder.arguments;

import io.github.llewvallis.commandbuilder.ArgumentParseException;
import io.github.llewvallis.commandbuilder.ArgumentParser;
import io.github.llewvallis.commandbuilder.CommandContext;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;

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
