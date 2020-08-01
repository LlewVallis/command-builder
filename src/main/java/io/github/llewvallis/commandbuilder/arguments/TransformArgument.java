package io.github.llewvallis.commandbuilder.arguments;

import io.github.llewvallis.commandbuilder.ArgumentParseException;
import io.github.llewvallis.commandbuilder.ArgumentParser;
import io.github.llewvallis.commandbuilder.CommandContext;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * A delegating argument which filters and maps the the result of
 * {@link ArgumentParser#parse(String, int, CommandContext)} using a provided function.
 *
 * This is used to power {@link ArgumentParser#map(Transformer)}.
 */
@AllArgsConstructor
public class TransformArgument<T, U> implements ArgumentParser<U> {

    private final ArgumentParser<T> underlying;
    private final Transformer<T, U> transformation;

    /**
     * Used to filter and map the result of {@link #parse(String, int, CommandContext)}.
     *
     * Throwing a {@link ArgumentParseException} allows a parsed value to be filtered out cleanly.
     */
    public interface Transformer<T, U> {

        U transform(T value) throws ArgumentParseException;
    }

    @Override
    public U parse(String argument, int position, CommandContext context) throws ArgumentParseException {
        return transformation.transform(underlying.parse(argument, position, context));
    }

    @Override
    public Set<String> complete(List<Object> parsedArguments, String currentArgument, int position, CommandContext context) {
        return underlying.complete(parsedArguments, currentArgument, position, context);
    }

    @Override
    public boolean isOptional() {
        return underlying.isOptional();
    }
}
