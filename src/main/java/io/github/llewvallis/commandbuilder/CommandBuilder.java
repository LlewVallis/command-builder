package io.github.llewvallis.commandbuilder;

import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility to quickly construct parsing logic for commands.
 */
public class CommandBuilder {

    private final List<ArgumentParser<?>> arguments = new ArrayList<>();
    private ArgumentParser<?> variadicArgument = null;
    private String usageMessage = null;

    private boolean constructed = false;
    private boolean canAddArgument = true;

    /**
     * Append a normal argument to the command.
     */
    public <T> CommandBuilder argument(ArgumentParser<T> argument) {
        assertNotConstructed();
        assertCanAddArgument();

        arguments.add(argument);

        return this;
    }

    /**
     * Set the variadic argument for the command.
     *
     * A variadic argument can be provided zero or more times. No further arguments, including variadic arguments, can
     * be provided after this operation.
     */
    public <T> CommandBuilder variadicArgument(ArgumentParser<T> argument) {
        assertNotConstructed();
        assertCanAddArgument();

        variadicArgument = argument;
        canAddArgument = false;

        return this;
    }

    /**
     * Infer arguments from an object that is usable with {@link ReflectionCommandCallback}.
     *
     * To use argument inference, each parameter of your execution method should be annotated with {@link Arg}. No
     * arguments, including variadic arguments, can be set before or after this operation.
     */
    public CommandBuilder infer(Object instance) {
        assertNotConstructed();

        if (arguments.size() != 0 || variadicArgument != null) {
            throw new IllegalStateException("arguments already added");
        }

        ReflectionCommandCallback.infer(this, instance);
        canAddArgument = false;

        return this;
    }

    /**
     * Set the usage message for the command.
     *
     * This message is provided in {@link CommandContext} to be displayed when a user executes a command with invalid
     * arguments. If this is not set, the value in the command context is inferred from the command's
     * {@link Command#getUsage()} property. Overriding the command's usage message can be useful, for example, when
     * creating a subcommand.
     */
    public CommandBuilder usageMessage(String message) {
        assertNotConstructed();

        usageMessage = message;

        return this;
    }

    /**
     * Create an executor which parses commands using the configured {@link ArgumentParser}s and delegates to the
     * provided callback.
     *
     * No other operations can follow this.
     */
    public TabExecutor build(CommandCallback callback) {
        constructed = true;
        return new BuiltExecutor(arguments, variadicArgument, callback, usageMessage);
    }

    /**
     * Has the same effect and return value as {@link #build(CommandCallback)}, except the provided command is
     * configured to use the built executor.
     */
    public TabExecutor build(CommandCallback callback, PluginCommand command) {
        TabExecutor executor = build(callback);
        command.setExecutor(executor);
        command.setTabCompleter(executor);
        return executor;
    }

    private void assertNotConstructed() {
        if (constructed) {
            throw new IllegalArgumentException("a command builder cannot be used after it has built");
        }
    }

    private void assertCanAddArgument() {
        if (!canAddArgument) {
            throw new IllegalArgumentException("no arguments can be added after a variadic argument");
        }
    }
}
