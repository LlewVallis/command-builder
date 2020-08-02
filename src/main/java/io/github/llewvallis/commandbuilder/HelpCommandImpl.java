package io.github.llewvallis.commandbuilder;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/* package-private */ class HelpCommandImpl extends SubCommand {

    private CompositeCommandBuilder compositeBuilder;

    public HelpCommandImpl(CompositeCommandBuilder builder) {
        this.compositeBuilder = builder;
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
                SubCommand subCommand = compositeBuilder.subCommands.get(argument);

                if (subCommand == null || !subCommand.getResolvedPermission(compositeBuilder.metadata)
                        .map(context.getSender()::hasPermission)
                        .orElse(true)) {
                    throw new ArgumentParseException("no such subcommand");
                }

                return subCommand;
            }

            @Override
            public Set<String> complete(List<Object> parsedArguments, String currentArgument, int position, CommandContext context) {
                return HelpCommandImpl.this.compositeBuilder.permittedSubCommands(context.getSender()).values().stream()
                        .map(SubCommand::getName)
                        .collect(Collectors.toSet());
            }
        }.optional());
    }

    @ExecuteCommand
    private void execute(CommandContext ctx, SubCommand subCommand) {
        if (subCommand == null) {
            compositeBuilder.showGeneralHelp(ctx.getSender());
        } else {
            compositeBuilder.showSpecificHelp(ctx.getSender(), subCommand);
        }
    }
}
