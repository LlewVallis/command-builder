package io.github.llewvallis.commandbuilder;

import io.github.llewvallis.commandbuilder.arguments.OptionalOverrideArgument;
import io.github.llewvallis.commandbuilder.arguments.TabOverrideArgument;
import io.github.llewvallis.commandbuilder.arguments.TransformArgument;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Parses command arguments as strings into useful values and provides possible tab completions.
 */
public interface ArgumentParser<T> {

    /**
     * Parse a command argument as a string into a useful value.
     *
     * @param position the index of the argument in relation to the entire command's arguments
     * @throws ArgumentParseException if the value could not be parsed
     */
    T parse(String argument, int position, CommandContext context) throws ArgumentParseException;

    /**
     * Provides a list of possible values for this argument to be used as tab completions.
     *
     * If the partially completed argument is not a prefix of a tab completion, it is not displayed to the user.
     *
     * @param parsedArguments the parsed values of all prior arguments
     * @param currentArgument the unfinished value of the argument being completed
     * @param position the index of the argument in relation to the entire command's arguments
     */
    Set<String> complete(List<Object> parsedArguments, String currentArgument, int position, CommandContext context);

    /**
     * Whether or not the argument can be omitted.
     *
     * An omitted argument is replaced with null when it is passed to the {@link CommandCallback}.
     */
    default boolean isOptional() {
        return false;
    }

    /**
     * A parser which wraps the current one, except that {@link #isOptional()} returns true.
     */
    default ArgumentParser<T> optional() {
        return new OptionalOverrideArgument<>(this, true);
    }

    /**
     * A parser which wraps the current one, except that {@link #complete(List, String, int, CommandContext)} fetches
     * its return value from the provided supplier.
     */
    default ArgumentParser<T> usingCompletions(Supplier<Set<String>> completions) {
        return new TabOverrideArgument<>(this, (parsedArguments, currentArgument, position, context, base) ->
                completions.get());
    }

    /**
     * A parser which wraps the current one, except that {@link #complete(List, String, int, CommandContext)} fetches
     * its return value from the provided supplier in addition to its normal completions.
     */
    default ArgumentParser<T> addCompletions(Supplier<Set<String>> completions) {
        return new TabOverrideArgument<>(this, (parsedArguments, currentArgument, position, context, base) -> {
            Set<String> result = new HashSet<>(base.complete(parsedArguments, currentArgument, position, context));
            result.addAll(completions.get());
            return result;
        });
    }

    /**
     * A parser which transforms any parsed value using the provided transformer.
     *
     * The transformer may throw a {@link ArgumentParseException} to filter out parsed values.
     */
    default <U> ArgumentParser<U> map(TransformArgument.Transformer<T, U> transformer) {
        return new TransformArgument<>(this, transformer);
    }
}
