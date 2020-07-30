package io.github.llewvallis.commandbuilder.arguments;

import io.github.llewvallis.commandbuilder.ArgumentParser;
import io.github.llewvallis.commandbuilder.CommandContext;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * An argument which matches any string.
 */
public class StringArgument implements ArgumentParser<String> {

    @Override
    public String parse(String argument, int position, CommandContext context) {
        return argument;
    }

    @Override
    public Set<String> complete(List<Object> parsedArguments, String currentArgument, int position, CommandContext context) {
        return Collections.emptySet();
    }
}
