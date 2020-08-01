package io.github.llewvallis.commandbuilder.arguments;

import io.github.llewvallis.commandbuilder.ArgumentParser;

/**
 * A delegating argument which overrides the {@link ArgumentParser#isOptional()} property with a provided value.
 *
 * This is used to power {@link ArgumentParser#optional()}.
 */
public class OptionalOverrideArgument<T> extends DelegateArgument<T> {

    private final boolean optional;

    public OptionalOverrideArgument(ArgumentParser<T> underlying, boolean optional) {
        super(underlying);
        this.optional = optional;
    }

    @Override
    public boolean isOptional() {
        return optional;
    }
}
