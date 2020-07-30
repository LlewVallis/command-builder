package io.github.llewvallis.commandbuilder;

import io.github.llewvallis.commandbuilder.arguments.StringSetArgument;
import lombok.*;
import lombok.experimental.Delegate;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Allows building commands which contain several subcommands, including a generated help command.
 */
public class CompositeCommandBuilder {

    private final Map<String, SubCommand> subCommands = new HashMap<>();

    private boolean constructed = false;

    private HelpMessageTheme theme = new HelpMessageTheme();

    /**
     * Describes the coloring applied to the help message.
     */
    @With
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HelpMessageTheme {
        ChatColor borderColor = ChatColor.YELLOW;
        ChatColor headingColor = ChatColor.WHITE;
        ChatColor labelColor = ChatColor.GOLD;
        ChatColor textColor = ChatColor.WHITE;
    }

    @Value
    private static class SubCommand {
        @Delegate
        TabExecutor executor;
        String description;
        String usage;
    }

    private class Executor implements TabExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String alias, String[] argumentStrings) {
            if (argumentStrings.length == 0) {
                showGeneralHelp(sender, alias);
                return true;
            }

            String subCommandName = argumentStrings[0];
            if (subCommands.containsKey(subCommandName)) {
                String[] subCommandArguments = Arrays.copyOfRange(argumentStrings, 1, argumentStrings.length);
                return subCommands.get(subCommandName).onCommand(sender, command, subCommandName, subCommandArguments);
            } else {
                TextComponent errorMessage = new TextComponent("That subcommand does not exist");
                errorMessage.setColor(ChatColor.RED);
                sender.spigot().sendMessage(errorMessage);

                return true;
            }
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] argumentStrings) {
            // Shouldn't happen, but handle it nicely just in-case
            if (argumentStrings.length == 0) {
                Bukkit.getLogger().warning("received zero length argument list when tab completing '" + alias + "'");
                return Collections.emptyList();
            }

            String subCommandName = argumentStrings[0];

            if (argumentStrings.length == 1) {
                return subCommands.keySet().stream()
                        .filter(subCommand -> subCommand.toLowerCase().startsWith(subCommandName.toLowerCase()))
                        .collect(Collectors.toList());
            }

            if (subCommands.containsKey(subCommandName)) {
                String[] subCommandArguments = Arrays.copyOfRange(argumentStrings, 1, argumentStrings.length);
                return subCommands.get(subCommandName).onTabComplete(sender, command, subCommandName, subCommandArguments);
            } else {
                return Collections.emptyList();
            }
        }
    }

    /**
     * Add a new normal subcommand to the builder.
     *
     * @param configurer used to configure the {@link CommandBuilder}, e.g. with arguments. The usage message is already
     *                   configured
     */
    public CompositeCommandBuilder command(String name, String description, String usage, Consumer<CommandBuilder> configurer, CommandCallback callback) {
        assertNotConstructed();

        CommandBuilder builder = new CommandBuilder();
        builder.usageMessage(usage);
        configurer.accept(builder);
        TabExecutor executor = builder.build(callback);

        subCommands.put(name, new SubCommand(executor, description, usage));

        return this;
    }

    /**
     * Add a new subcommand, containing nested subcommands to the builder.
     *
     * @param configurer used to configure the {@link CompositeCommandBuilder}, e.g. with commands. The builder inherits
     *                   the current {@link HelpMessageTheme}
     */
    public CompositeCommandBuilder nest(String name, String description, Consumer<CompositeCommandBuilder> configurer) {
        assertNotConstructed();

        CompositeCommandBuilder builder = new CompositeCommandBuilder();
        builder.helpMessageTheme(theme);
        configurer.accept(builder);
        TabExecutor executor = builder.build();

        subCommands.put(name, new SubCommand(executor, description, null));

        return this;
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
     * Create an executor which handles delegating to subcommands.
     */
    public TabExecutor build() {
        assertNotConstructed();

        if (!subCommands.containsKey("help")) {
            Set<String> availableSubCommandNames = new HashSet<>(subCommands.keySet());
            availableSubCommandNames.add("help");

            command(
                    "help",
                    "Show help for subcommands",
                    "help [subcommand]",
                    builder -> builder.argument(new StringSetArgument(availableSubCommandNames).optional()),
                    new ReflectionCommandCallback(new Object() {
                        @ExecuteCommand
                        public void execute(CommandContext ctx, String subCommand) {
                            if (subCommand == null) {
                                showGeneralHelp(ctx.sender, ctx.alias);
                            } else {
                                showSpecificHelp(ctx.sender, subCommand);
                            }
                        }
                    })
            );
        }

        constructed = true;
        return new Executor();
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
            String newUsage = command.getLabel() + " <" + String.join("|", subCommands.keySet()) + ">";
            command.setUsage(newUsage);
        }

        return executor;
    }

    private void showGeneralHelp(CommandSender sender, String alias) {
        ComponentBuilder message = createHelpHeader(alias);

        subCommands.entrySet().stream()
                .filter(entry -> entry.getValue().usage == null)
                .forEach(entry -> {
                    message.append("\n" + entry.getKey() + ": ").color(theme.labelColor);
                    message.append(shortenDescription(entry.getValue().description)).color(theme.textColor);
                });

        subCommands.values().stream()
                .filter(subCommand -> subCommand.usage != null)
                .forEach(subCommand -> {
                    message.append("\n" + subCommand.usage + ": ").color(theme.labelColor);
                    message.append(shortenDescription(subCommand.description)).color(theme.textColor);
                });

        sender.spigot().sendMessage(message.create());
    }

    private void showSpecificHelp(CommandSender sender, String subCommandName) {
        SubCommand subCommand = subCommands.get(subCommandName);
        ComponentBuilder message = createHelpHeader(subCommandName);

        message.append("\nDescription: ").color(theme.labelColor);
        message.append(subCommand.description).color(theme.textColor);

        if (subCommand.usage != null) {
            message.append("\nUsage: ").color(theme.labelColor);
            message.append(subCommand.usage).color(theme.textColor);
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

    private void assertNotConstructed() {
        if (constructed) {
            throw new IllegalArgumentException("a composite command builder cannot be used after it has built");
        }
    }
}
