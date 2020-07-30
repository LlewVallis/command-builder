package io.github.llewvallis.commandbuilder;

/**
 * Thrown when a command could not be parsed.
 *
 * Contains a user facing message detailing why parsing failed.
 */
public class CommandParseException extends Exception {

    public CommandParseException(String message) {
        super(message);
    }
}
