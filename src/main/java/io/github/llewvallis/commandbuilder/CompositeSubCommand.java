package io.github.llewvallis.commandbuilder;

import java.util.Optional;

/**
 * A composite command which is nested within a {@link CompositeCommandBuilder}.
 *
 * Nesting an instance of this class can be achieved using the {@link CompositeCommandBuilder#nest(CompositeSubCommand)}
 * method.
 */
public abstract class CompositeSubCommand {

    public abstract String getName();

    public abstract String getDescription();

    /**
     * Configure the composite command builder by, for example, adding commands.
     *
     * By default this does nothing, but can be overridden if needed. Note that the theme of the parent composite
     * builder will be set for the child builder.
     */
    protected void configure(CompositeCommandBuilder builder) { }

    /**
     * An optional permission which will additionally be required for the subcommand and its children.
     */
    public Optional<String> getPermission() {
        return Optional.empty();
    }
}
