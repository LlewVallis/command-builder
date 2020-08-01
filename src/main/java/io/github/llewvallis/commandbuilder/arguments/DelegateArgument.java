package io.github.llewvallis.commandbuilder.arguments;

import io.github.llewvallis.commandbuilder.ArgumentParseException;
import io.github.llewvallis.commandbuilder.ArgumentParser;
import io.github.llewvallis.commandbuilder.CommandContext;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * An argument which delegates {@link #parse(String, int, CommandContext)},
 * {@link #complete(List, String, int, CommandContext)} and {@link #isOptional()} to an underlying instance.
 */
@RequiredArgsConstructor
public class DelegateArgument<T> implements ArgumentParser<T> {

    private final ArgumentParser<T> underlying;

    @Override
    public T parse(String argument, int position, CommandContext context) throws ArgumentParseException {
        return underlying.parse(argument, position, context);
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
