package io.github.llewvallis.commandbuilder;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/* package-private */ class HelpCommandImpl extends SubCommand {

    private CompositeCommandBuilder builder;

    public HelpCommandImpl(CompositeCommandBuilder builder) {
        this.builder = builder;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Show help for subcommands";
    }

    @Override
    public String getUsageMessage() {
        return "help [subcommand]";
    }

    @Override
    protected void configure(CommandBuilder builder) {
        builder.argument(new ArgumentParser<SubCommand>() {
            @Override
            public SubCommand parse(String argument, int position, CommandContext context) throws ArgumentParseException {
                SubCommand subCommand = HelpCommandImpl.this.builder.subCommands.get(argument);

                if (subCommand == null || !subCommand.getPermission()
                        .map(context.getSender()::hasPermission)
                        .orElse(true)) {
                    throw new ArgumentParseException("no such subcommand");
                }

                return subCommand;
            }

            @Override
            public Set<String> complete(List<Object> parsedArguments, String currentArgument, int position, CommandContext context) {
                return HelpCommandImpl.this.builder.permittedSubCommands(context.getSender()).values().stream()
                        .map(SubCommand::getName)
                        .collect(Collectors.toSet());
            }
        }.optional());
    }

    @ExecuteCommand
    private void execute(CommandContext ctx, SubCommand subCommand) {
        if (subCommand == null) {
            builder.showGeneralHelp(ctx.getSender());
        } else {
            builder.showSpecificHelp(ctx.getSender(), subCommand);
        }
    }
}
