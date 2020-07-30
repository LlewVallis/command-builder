package io.github.llewvallis.commandbuilder.arguments;

import io.github.llewvallis.commandbuilder.ArgumentParseException;
import io.github.llewvallis.commandbuilder.ArgumentParser;
import io.github.llewvallis.commandbuilder.CommandContext;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * A delegating argument which overrides the result of
 * {@link ArgumentParser#complete(List, String, int, CommandContext)} using a provided function.
 *
 * This is used to power {@link ArgumentParser#usingCompletions(Supplier)} and
 * {@link ArgumentParser#addCompletions(Supplier)}.
 */
public class TabOverrideArgument<Result, BaseArgument extends ArgumentParser<Result>> implements ArgumentParser<Result> {

    private final BaseArgument baseArgument;
    private final CompleteFunction<Result, BaseArgument> completeFunction;

    public TabOverrideArgument(BaseArgument baseArgument, CompleteFunction<Result, BaseArgument> completeFunction) {
        this.baseArgument = baseArgument;
        this.completeFunction = completeFunction;
    }

    /**
     * Used to provide the result of {@link #usingCompletions(Supplier)}.
     */
    public interface CompleteFunction<Result, BaseArgument extends ArgumentParser<Result>> {

        Set<String> complete(List<Object> parsedArguments, String currentArgument, int position, CommandContext context,
                             BaseArgument baseArgument);
    }

    @Override
    public Result parse(String argument, int position, CommandContext context) throws ArgumentParseException {
        return baseArgument.parse(argument, position, context);
    }

    @Override
    public Set<String> complete(List<Object> parsedArguments, String currentArgument, int position, CommandContext context) {
        return completeFunction.complete(parsedArguments, currentArgument, position, context, baseArgument);
    }

    @Override
    public boolean isOptional() {
        return baseArgument.isOptional();
    }
}
