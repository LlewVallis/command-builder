package io.github.llewvallis.commandbuilder;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/* package-private */ class CompositeCommandImpl implements TabExecutor {

    private CompositeCommandBuilder compositeCommandBuilder;

    public CompositeCommandImpl(CompositeCommandBuilder compositeCommandBuilder) {
        this.compositeCommandBuilder = compositeCommandBuilder;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] argumentStrings) {
        if (argumentStrings.length == 0) {
            compositeCommandBuilder.noArgsAction.accept(sender);
            return true;
        }

        Map<String, SubCommand> permittedSubCommands = compositeCommandBuilder.permittedSubCommands(sender);

        String subCommandName = argumentStrings[0];
        if (permittedSubCommands.containsKey(subCommandName)) {
            String[] subCommandArguments = Arrays.copyOfRange(argumentStrings, 1, argumentStrings.length);
            SubCommand subCommand = permittedSubCommands.get(subCommandName);
            return subCommand.getOrCreateExecutor().onCommand(sender, command, subCommandName, subCommandArguments);
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
        Map<String, SubCommand> permittedSubCommands = compositeCommandBuilder.permittedSubCommands(sender);

        if (argumentStrings.length == 1) {
            return permittedSubCommands.keySet().stream()
                    .filter(subCommand -> subCommand.toLowerCase().startsWith(subCommandName.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (permittedSubCommands.containsKey(subCommandName)) {
            String[] subCommandArguments = Arrays.copyOfRange(argumentStrings, 1, argumentStrings.length);
            SubCommand subCommand = permittedSubCommands.get(subCommandName);
            return subCommand.getOrCreateExecutor().onTabComplete(sender, command, subCommandName, subCommandArguments);
        } else {
            return Collections.emptyList();
        }
    }
}
