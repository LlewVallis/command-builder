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
public class TabOverrideArgument<T, U extends ArgumentParser<T>> implements ArgumentParser<T> {

    private final U underlying;
    private final CompleteFunction<T, U> completeFunction;

    public TabOverrideArgument(U underlying, CompleteFunction<T, U> completeFunction) {
        this.underlying = underlying;
        this.completeFunction = completeFunction;
    }

    /**
     * Used to provide the result of {@link #usingCompletions(Supplier)}.
     */
    public interface CompleteFunction<T, U extends ArgumentParser<T>> {

        Set<String> complete(List<Object> parsedArguments, String currentArgument, int position, CommandContext context,
                             U underlying);
    }

    @Override
    public T parse(String argument, int position, CommandContext context) throws ArgumentParseException {
        return underlying.parse(argument, position, context);
    }

    @Override
    public Set<String> complete(List<Object> parsedArguments, String currentArgument, int position, CommandContext context) {
        return completeFunction.complete(parsedArguments, currentArgument, position, context, underlying);
    }

    @Override
    public boolean isOptional() {
        return underlying.isOptional();
    }
}
