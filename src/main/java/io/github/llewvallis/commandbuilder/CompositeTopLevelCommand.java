package io.github.llewvallis.commandbuilder;

import org.bukkit.command.TabExecutor;

/**
 * A composite command eligible for auto registration.
 */
public abstract class CompositeTopLevelCommand {

    private TabExecutor executor = null;

    /**
     * The name and label of the command to be registered.
     */
    public abstract String getName();

    /**
     * Configure the composite command builder by, for example, adding commands.
     *
     * By default this does nothing, but can be overridden if needed.
     */
    protected void configure(CompositeCommandBuilder builder) { }

    /* package-private */ TabExecutor getOrCreateExecutor() {
        if (executor == null) {
            CompositeCommandBuilder builder = new CompositeCommandBuilder();
            configure(builder);
            executor = builder.build();
        }

        return executor;
    }
}
