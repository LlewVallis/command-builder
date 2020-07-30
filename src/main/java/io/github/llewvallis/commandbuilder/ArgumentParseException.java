package io.github.llewvallis.commandbuilder;

/**
 * Raised within an {@link ArgumentParser} when an argument could not be parsed.
 *
 * The message associated with this exception is displayed to the user if failure to parse the argument resulted in
 * failure to parse the entire command.
 */
public class ArgumentParseException extends Exception {

    public ArgumentParseException(String message) {
        super(message);
    }
}
