package io.github.llewvallis.commandbuilder;

import java.util.Optional;

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
