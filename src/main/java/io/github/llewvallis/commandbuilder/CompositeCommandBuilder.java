package io.github.llewvallis.commandbuilder;

import lombok.*;
import lombok.extern.java.Log;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.*;
import java.util.function.Consumer;

/**
 * Allows building commands which contain several subcommands, including a generated help command.
 */
@Log
public class CompositeCommandBuilder {

    /* package-private */ final Map<String, SubCommand> subCommands = new HashMap<>();

    private boolean constructed = false;

    /* package-private */ HelpMessageTheme theme = new HelpMessageTheme();
    /* package-private */ Consumer<CommandSender> noArgsAction = this::showGeneralHelp;
    /* package-private */ Map<String, Object> metadata = new HashMap<>();

    /**
     * Describes the coloring applied to the help message.
     */
    @With
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HelpMessageTheme {

        /**
         * The color used for the hyphens in the help message header.
         */
        ChatColor borderColor = ChatColor.YELLOW;

        /**
         * The color used for the heading text.
         */
        ChatColor headingColor = ChatColor.WHITE;

        /**
         * The color used for labels which precede descriptions and other long form information.
         */
        ChatColor labelColor = ChatColor.GOLD;

        /**
         * The color used for descriptions and other long form information.
         */
        ChatColor textColor = ChatColor.WHITE;
    }

    /**
     * Add a new normal subcommand to the builder.
     */
    public CompositeCommandBuilder command(SubCommand subCommand) {
        assertNotConstructed();
        subCommands.put(subCommand.getName(), subCommand);
        return this;
    }

    /**
     * Add a new composite subcommand, containing nested subcommands, to the builder.
     */
    public CompositeCommandBuilder nest(CompositeSubCommand subCommand) {
        assertNotConstructed();
        return command(new CompositeSubCommandImpl(this, subCommand));
    }

    /**
     * Set the help message theme used when displaying the help message.
     */
    public CompositeCommandBuilder helpMessageTheme(HelpMessageTheme theme) {
        assertNotConstructed();
        this.theme = theme;
        return this;
    }

    /**
     * Override the behaviour for when the command is invoked without arguments.
     */
    public CompositeCommandBuilder onEmptyInvocation(Consumer<CommandSender> action) {
        assertNotConstructed();
        noArgsAction = action;
        return this;
    }

    /**
     * Specify a {@link PluginDescriptionFile} which will be used to fill in description, usage and permission
     * properties for subcommands if they are not present. This is automatically set during auto registration.
     */
    public CompositeCommandBuilder metadata(PluginDescriptionFile description, String name) {
        assertNotConstructed();
        metadata = Objects.requireNonNullElse(description.getCommands().get(name), Map.of());
        return this;
    }

    /**
     * Create an executor which handles delegating to subcommands.
     */
    public TabExecutor build() {
        assertNotConstructed();

        if (!subCommands.containsKey("help")) {
            command(new HelpCommandImpl(this));
        }

        constructed = true;
        return new CompositeCommandImpl(this);
    }

    /**
     * Has the same effect and return value as {@link #build()}, except the provided command is configured to use the
     * built executor.
     */
    public TabExecutor build(PluginCommand command) {
        TabExecutor executor = build();

        command.setExecutor(executor);
        command.setTabCompleter(executor);

        if (command.getUsage().isBlank()) {
            command.setUsage(getDefaultUsage(command.getLabel()));
        }

        return executor;
    }

    /* package-private */ static String getDefaultUsage(String name) {
        return name + " <subcommand>";
    }

    /* package-private */ static Map<String, Object> getSubCommandMetadata(Map<String, Object> metadata, String name) {
        Object subCommandsObject = metadata.get("subcommands");
        if (subCommandsObject instanceof Map) {
            Object subCommandObject = ((Map) subCommandsObject).get(name);

            if (subCommandObject instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> subCommandMetadata = (Map<String, Object>) subCommandObject;
                return subCommandMetadata;
            } else if (subCommandObject == null) {
                return Map.of();
            } else {
                log.warning("Expected object under property subcommands." + name + " but found " + subCommandObject);
                return Map.of();
            }
        } else if (subCommandsObject == null) {
            return Map.of();
        } else {
            log.warning("Expected object under property subcommands but found " + subCommandsObject);
            return Map.of();
        }
    }

    /* package-private */ void showGeneralHelp(CommandSender sender) {
        ComponentBuilder message = createHelpHeader("all");

        Collection<SubCommand> permittedSubCommands = permittedSubCommands(sender).values();

        permittedSubCommands.stream()
                .filter(SubCommand::isNested)
                .forEach(subCommand -> {
                    String description = subCommand.getResolvedDescription(metadata);

                    message.append("\n" + subCommand.getName()).color(theme.labelColor);
                    message.append("\n\u2514 " + shortenDescription(description)).color(theme.textColor);
                });

        permittedSubCommands.stream()
                .filter(subCommand -> !subCommand.isNested())
                .forEach(subCommand -> {
                    String usage = subCommand.getResolvedUsageMessage(metadata);
                    String description = subCommand.getResolvedDescription(metadata);

                    message.append("\n" + usage).color(theme.labelColor);
                    message.append("\n\u2514 " + shortenDescription(description)).color(theme.textColor);
                });

        sender.spigot().sendMessage(message.create());
    }

    /* package-private */ void showSpecificHelp(CommandSender sender, SubCommand subCommand) {
        ComponentBuilder message = createHelpHeader(subCommand.getName());

        String description = subCommand.getResolvedDescription(metadata);
        message.append("\nDescription: ").color(theme.labelColor);
        message.append(description).color(theme.textColor);

        if (!subCommand.isNested()) {
            String usage = subCommand.getResolvedUsageMessage(metadata);
            message.append("\nUsage: ").color(theme.labelColor);
            message.append(usage).color(theme.textColor);
        }

        sender.spigot().sendMessage(message.create());
    }

    private ComponentBuilder createHelpHeader(String topic) {
        String headerPrefix = "--------- ";
        String header = "Help: " + topic;
        String headerSuffix = " " + "-".repeat(Math.max(0, 49 - headerPrefix.length() - header.length()));

        ComponentBuilder message = new ComponentBuilder();
        message.append(headerPrefix).color(theme.borderColor);
        message.append(header).color(theme.headingColor);
        message.append(headerSuffix).color(theme.borderColor);

        return message;
    }

    private String shortenDescription(String description) {
        if (description.length() > 50) {
            return description.substring(0, 47) + "...";
        } else {
            return description;
        }
    }

    /* package-private */ Map<String, SubCommand> permittedSubCommands(CommandSender sender) {
        Map<String, SubCommand> permittedSubCommands = new HashMap<>(subCommands);
        permittedSubCommands.values().removeIf(subCommand ->
                subCommand.getResolvedPermission(metadata)
                        .map(perm -> !sender.hasPermission(perm))
                        .orElse(false)
        );
        return permittedSubCommands;
    }

    private void assertNotConstructed() {
        if (constructed) {
            throw new IllegalArgumentException("a composite command builder cannot be used after it has built");
        }
    }
}
