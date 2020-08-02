package io.github.llewvallis.commandbuilder;

import org.bukkit.command.TabExecutor;

import java.util.Map;
import java.util.Optional;

/* package-private */ class CompositeSubCommandImpl extends SubCommand {

    private final CompositeSubCommand subCommand;
    private final TabExecutor executor;

    public CompositeSubCommandImpl(CompositeCommandBuilder parent, CompositeSubCommand subCommand) {
        this.subCommand = subCommand;

        CompositeCommandBuilder builder = new CompositeCommandBuilder();

        builder.helpMessageTheme(parent.theme);
        builder.metadata = CompositeCommandBuilder.getSubCommandMetadata(parent.metadata, getName());

        subCommand.configure(builder);
        executor = builder.build();
    }

    @Override
    /* package-private */ TabExecutor getOrCreateExecutor(Map<String, Object> metadata) {
        return executor;
    }

    @Override
    boolean isNested() {
        return true;
    }

    @Override
    public String getName() {
        return subCommand.getName();
    }

    @Override
    public String getDescription() {
        return subCommand.getDescription();
    }

    @Override
    public Optional<String> getPermission() {
        return subCommand.getPermission();
    }

    @Override
    public String getUsageMessage() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void configure(CommandBuilder builder) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected CommandCallback getCallback() {
        throw new UnsupportedOperationException();
    }
}
