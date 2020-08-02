package io.github.llewvallis.commandbuilder;

import java.util.Optional;

/**
 * A composite command which is nested within a {@link CompositeCommandBuilder}.
 *
 * Nesting an instance of this class can be achieved using the {@link CompositeCommandBuilder#nest(CompositeSubCommand)}
 * method or through auto registration.
 */
public abstract class CompositeSubCommand {

    /**
     * The name and label of the subcommand.
     */
    public abstract String getName();

    /**
     * The description of the subcommand.
     *
     * If this throws an {@link InferFromMetadataException} the description will be fetched from the
     * {@link CompositeCommandBuilder}'s metadata.
     */
    public String getDescription() {
        throw new InferFromMetadataException();
    }

    /**
     * An optional permission which will additionally be required for the subcommand and its children.
     *
     * If this throws an {@link InferFromMetadataException} the permission will be fetched from the
     * {@link CompositeCommandBuilder}'s metadata. Note that returning {@link Optional#empty()} from this method will
     * cause permission metadata to be ignored.
     */
    public Optional<String> getPermission() {
        throw new InferFromMetadataException();
    }

    /**
     * Configure the composite command builder by, for example, adding commands.
     *
     * By default this does nothing, but can be overridden if needed. Note that the theme of the parent composite
     * builder will be set for the child builder.
     */
    protected void configure(CompositeCommandBuilder builder) { }

}
