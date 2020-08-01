package io.github.llewvallis.commandbuilder.arguments;

import io.github.llewvallis.commandbuilder.ArgumentParseException;
import io.github.llewvallis.commandbuilder.ArgumentParser;
import io.github.llewvallis.commandbuilder.CommandContext;
import io.github.llewvallis.commandbuilder.ParserAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Set;

/**
 * An argument that matches only a set of whitelisted strings that are also used as tab completions.
 */
public class StringSetArgument implements ArgumentParser<String> {

    private final Set<String> possibleValues;

    public StringSetArgument(Set<String> possibleValues) {
        this.possibleValues = possibleValues;
    }

    public StringSetArgument(String... possibleValues) {
        this(Set.of(possibleValues));
    }

    @ParserAnnotation(StringSetArgument.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    public @interface Arg {

        String[] value();
    }

    private static StringSetArgument createParserFromAnnotation(Arg arg) {
        return new StringSetArgument(arg.value());
    }

    @Override
    public String parse(String argument, int position, CommandContext context) throws ArgumentParseException {
        if (possibleValues.contains(argument)) {
            return argument;
        } else {
            throw new ArgumentParseException("expected one of " + possibleValues + " but found '" + argument + "'");
        }
    }

    @Override
    public Set<String> complete(List<Object> parsedArguments, String currentArgument, int position, CommandContext context) {
        return possibleValues;
    }
}
