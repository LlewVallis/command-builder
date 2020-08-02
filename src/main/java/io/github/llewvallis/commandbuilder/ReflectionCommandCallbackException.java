package io.github.llewvallis.commandbuilder;

/**
 * Thrown when a reflective operation fails from, for example, a method being missing or a constructor throwing an
 * exception.
 *
 * This is most commonly thrown from {@link ReflectionCommandCallback} invoking a command, but it can also be thrown
 * when performing auto registration or logged during argument inference.
 */
public class ReflectionCommandCallbackException extends RuntimeException {

    public ReflectionCommandCallbackException(String message) {
        super(message);
    }

    public ReflectionCommandCallbackException(String message, Throwable cause) {
        super(message, cause);
    }
}
