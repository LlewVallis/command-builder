package io.github.llewvallis.commandbuilder.arguments;

import io.github.llewvallis.commandbuilder.CommandContext;
import io.github.llewvallis.commandbuilder.ArgumentParseException;
import io.github.llewvallis.commandbuilder.ArgumentParser;

import java.util.List;
import java.util.Set;

/**
 * A delegating argument which overrides the {@link ArgumentParser#isOptional()} property with a provided value.
 *
 * This is used to power {@link ArgumentParser#optional()}.
 */
public class OptionalOverrideArgument<Result, BaseArgument extends ArgumentParser<Result>> implements ArgumentParser<Result> {

    private final BaseArgument baseArgument;
    private final boolean optional;

    public OptionalOverrideArgument(BaseArgument baseArgument, boolean optional) {
        this.baseArgument = baseArgument;
        this.optional = optional;
    }

    @Override
    public Result parse(String argument, int position, CommandContext context) throws ArgumentParseException {
        return baseArgument.parse(argument, position, context);
    }

    @Override
    public Set<String> complete(List<Object> parsedArguments, String currentArgument, int position, CommandContext context) {
        return baseArgument.complete(parsedArguments, currentArgument, position, context);
    }

    @Override
    public boolean isOptional() {
        return optional;
    }
}
