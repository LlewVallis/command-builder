package io.github.llewvallis.commandbuilder;

import java.util.List;

/**
 * Receives the parsed command arguments and executes the command.
 */
public interface CommandCallback {

    /**
     * Called when a command is successfully parsed and should be executed.
     *
     * Throwing a {@link CommandParseException} from this method will trigger the
     * {@link #onFailure(CommandParseException, CommandContext)} method.
     */
    void onSuccess(List<Object> argumentValues, List<Object> variadicArgumentValues, CommandContext context);

    /**
     * Called when a command could not be parsed and an error message should be displayed.
     */
    void onFailure(CommandParseException cause, CommandContext context);
}
