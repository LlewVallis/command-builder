package io.github.llewvallis.commandbuilder.arguments;

import io.github.llewvallis.commandbuilder.CommandContext;
import io.github.llewvallis.commandbuilder.ArgumentParseException;
import io.github.llewvallis.commandbuilder.ArgumentParser;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;

import java.util.List;
import java.util.Set;

/**
 * An argument that matches optionally bounded integers.
 */
@With
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IntegerArgument implements ArgumentParser<Integer> {

    private int min = Integer.MIN_VALUE;
    private int max = Integer.MAX_VALUE;

    @Override
    public Integer parse(String argument, int position, CommandContext context) throws ArgumentParseException {
        int value;
        try {
            value = Integer.parseInt(argument);
        } catch (NumberFormatException e) {
            throw new ArgumentParseException("not a valid integer");
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
