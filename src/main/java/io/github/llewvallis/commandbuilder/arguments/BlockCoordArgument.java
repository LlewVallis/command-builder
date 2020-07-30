package io.github.llewvallis.commandbuilder.arguments;

import io.github.llewvallis.commandbuilder.ArgumentParseException;
import io.github.llewvallis.commandbuilder.ArgumentParser;
import io.github.llewvallis.commandbuilder.CommandContext;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.entity.Entity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * An argument that matches block X, Y and Z coordinates.
 *
 * If the command sender has a position, that position is provided as a tab complete and relative coordinates can be
 * used.
 */
public class BlockCoordArgument implements ArgumentParser<Integer> {

    private final Axis axis;

    public BlockCoordArgument(Axis axis) {
        this.axis = axis;
    }

    public enum Axis {
        X, Y, Z
    }

    @Override
    public Integer parse(String argument, int position, CommandContext context) throws ArgumentParseException {
        boolean relative = argument.startsWith("~");
        if (relative) {
            argument = argument.substring(1);
        }

        int value;
        if (relative && argument.equals("")) {
            value = 0;
        } else {
            try {
                value = Integer.parseInt(argument);
            } catch (NumberFormatException e) {
                throw new ArgumentParseException("invalid coordinate");
            }
        }

        if (relative) {
            value += getSenderCoord(context.sender).orElseThrow(() ->
                    new ArgumentParseException("cannot use relative coordinates in this context"));
        }

        return value;
    }

    @Override
    public Set<String> complete(List<Object> parsedArguments, String currentArgument, int position, CommandContext context) {
        return getSenderCoord(context.sender)
                .map(Object::toString)
                .map(Set::of)
                .orElse(Collections.emptySet());
    }

    private Optional<Integer> getSenderCoord(CommandSender sender) {
        if (sender instanceof Entity)  {
            Location location = ((Entity) sender).getLocation();

            switch (axis) {
                case X:
                    return Optional.of(location.getBlockX());
                case Y:
                    return Optional.of(location.getBlockY());
                case Z:
                    return Optional.of(location.getBlockZ());
            }
        }

        if (sender instanceof BlockCommandSender) {
            Block block = ((BlockCommandSender) sender).getBlock();

            switch (axis) {
                case X:
                    return Optional.of(block.getX());
                case Y:
                    return Optional.of(block.getY());
                case Z:
                    return Optional.of(block.getZ());
            }
        }

        if (sender instanceof ProxiedCommandSender) {
            return getSenderCoord(((ProxiedCommandSender) sender).getCallee());
        }

        return Optional.empty();
    }
}
