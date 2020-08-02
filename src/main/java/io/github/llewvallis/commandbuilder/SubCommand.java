package io.github.llewvallis.commandbuilder;

import lombok.extern.java.Log;
import org.bukkit.command.TabExecutor;

import java.util.Map;
import java.util.Optional;

/**
 * A subcommand which can be registered into a {@link CompositeCommandBuilder}.
 *
 * Can be used not only to execute the subcommand, but provides metadata such as a description as well. Classes
 * inheriting from this can be also be used for auto subcommand registration.
 */
@Log
public abstract class SubCommand {

    private TabExecutor executor = null;

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
     * The usage message of the subcommand.
     *
     * If this throws an {@link InferFromMetadataException} the usage message will be fetched from the
     * {@link CompositeCommandBuilder}'s metadata.
     */
    public String getUsageMessage() {
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
     * Configure the command builder by, for example, adding arguments.
     *
     * By default this delegates to {@link CommandBuilder#infer(Object)} with the current instance, but that can be
     * overridden if needs be.
     */
    protected void configure(CommandBuilder builder) {
        builder.infer(this);
    }

    /**
     * Create a command callback used for {@link CommandBuilder#build(CommandCallback)}.
     *
     * By default this creates a {@link ReflectionCommandCallback} using the current instance, but in rare cases this
     * can be overridden.
     */
    protected CommandCallback getCallback() {
        return new ReflectionCommandCallback(this);
    }

    /* package-private */ String getResolvedDescription(Map<String, Object> metadata) {
        try {
            return getDescription();
        } catch (InferFromMetadataException e) {
            String metadataDescription = getMetadataString(metadata, "description");
            if (metadataDescription != null) {
                return metadataDescription;
            }

            log.warning("Missing description metadata for " + this);
            return "Description for " + getName();
        }
    }

    /* package-private */ String getResolvedUsageMessage(Map<String, Object> metadata) {
        try {
            return getUsageMessage();
        } catch (InferFromMetadataException e) {
            String metadataUsage = getMetadataString(metadata, "usage");
            if (metadataUsage != null) {
                return metadataUsage;
            }

            log.warning("Missing usage metadata for " + this);
            return getName();
        }
    }

    /* package-private */ Optional<String> getResolvedPermission(Map<String, Object> metadata) {
        try {
            return getPermission();
        } catch (InferFromMetadataException e) {
            String metadataPermission = getMetadataString(metadata, "permission");
            return Optional.ofNullable(metadataPermission);
        }
    }

    private String getMetadataString(Map<String, Object> parentMetadata, String property) {
        Map<String, Object> metadata = CompositeCommandBuilder.getSubCommandMetadata(parentMetadata, getName());

        Object propertyValue = metadata.get(property);
        if (propertyValue instanceof String) {
            return (String) propertyValue;
        } else if (propertyValue == null) {
            return null;
        } else {
            log.warning("Expected string under property " + getName() + "." + property + " but found " + propertyValue);
            return null;
        }
    }

    /* package-private */ TabExecutor getOrCreateExecutor(Map<String, Object> metadata) {
        if (executor == null) {
            CommandBuilder builder = new CommandBuilder();
            builder.usageMessage(getResolvedUsageMessage(metadata));
            configure(builder);
            executor = builder.build(getCallback());
        }

        return executor;
    }

    /* package-private */ boolean isNested() {
        return false;
    }
}
