package io.github.llewvallis.commandbuilder;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Context about the execution or tab completion of a command.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class CommandContext {

    /**
     * The entity (or non-entity) which sent the command.
     */
    private final CommandSender sender;

    /**
     * The root command that was sent.
     */
    private final Command command;

    /**
     * The alias or subcommand name used to send the command.
     */
    private final String alias;

    /**
     * A raw list of the arguments passed to the command.
     *
     * This list should not be modified.
     */
    private final List<String> argumentStrings;

    /**
     * The usage message which should be displayed if parsing failed.
     *
     * Defaults to {@link Command#getUsage()} but can be overridden.
     */
    private final String usageMessage;
}
