package io.github.llewvallis.commandbuilder;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.With;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Context about the execution or tab completion of a command.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class CommandContext {

    public final CommandSender sender;
    public final Command command;
    public final String alias;

    /**
     * A raw list of the arguments passed to the command.
     *
     * This list should not be modified.
     */
    public final List<String> argumentStrings;

    /**
     * The usage message which should be displayed if parsing failed.
     *
     * Defaults to {@link Command#getUsage()} but can be overridden.
     */
    public final String usageMessage;
}
