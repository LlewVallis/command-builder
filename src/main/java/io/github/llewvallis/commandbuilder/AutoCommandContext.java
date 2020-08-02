package io.github.llewvallis.commandbuilder;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;

/**
 * Context about the instantiation of an auto command.
 *
 * Can from a factory method specified in {@link AutoCommand}.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class AutoCommandContext {

    /**
     * The plugin provided when triggering auto registration.
     */
    private final Plugin plugin;
}
