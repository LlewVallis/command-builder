package io.github.llewvallis.commandbuilder;

import org.bukkit.command.TabExecutor;

/**
 * A command eligible for auto registration.
 */
public abstract class TopLevelCommand {

    private TabExecutor executor = null;

    /**
     * The name and label of the command to be registered.
     */
    public abstract String getName();

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

    /* package-private */ TabExecutor getOrCreateExecutor() {
        if (executor == null) {
            CommandBuilder builder = new CommandBuilder();
            configure(builder);
            executor = builder.build(getCallback());
        }

        return executor;
    }
}
