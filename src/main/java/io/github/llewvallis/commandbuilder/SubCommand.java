package io.github.llewvallis.commandbuilder;

import org.bukkit.command.TabExecutor;

import java.util.Optional;

/**
 * A subcommand which can be registered into a {@link CompositeCommandBuilder}.
 *
 * Can be used not only to execute the subcommand, but provides metadata such as a description as well.
 */
public abstract class SubCommand {

    private TabExecutor executor = null;

    public abstract String getName();

    public abstract String getDescription();

    public abstract String getUsageMessage();

    /**
     * Configure the command builder by, for example, adding arguments.
     *
     * By default this does nothing, but can be overridden if needed. Note that the usage message will already be
     * configured for the builder.
     */
    protected void configure(CommandBuilder builder) { }

    /**
     * Create a command callback used for {@link CommandBuilder#build(CommandCallback)}.
     *
     * By default this creates a {@link ReflectionCommandCallback} using the current instance, but in rare cases this
     * can be overridden.
     */
    protected CommandCallback getCallback() {
        return new ReflectionCommandCallback(this);
    }

    /**
     * An optional permission which will additionally be required for the subcommand.
     */
    public Optional<String> getPermission() {
        return Optional.empty();
    }

    /* package-private */ TabExecutor getOrCreateExecutor() {
        if (executor == null) {
            CommandBuilder builder = new CommandBuilder();
            builder.usageMessage(getUsageMessage());
            configure(builder);
            executor = builder.build(getCallback());
        }

        return executor;
    }

    /* package-private */ boolean isNested() {
        return false;
    }
}
