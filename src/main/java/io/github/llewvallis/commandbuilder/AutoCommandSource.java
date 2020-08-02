package io.github.llewvallis.commandbuilder;

import java.util.Set;

/**
 * Provides a source of classes to be scanned during auto command registration.
 */
public interface AutoCommandSource {

    /**
     * A set of potential candidates for auto registration.
     *
     * This method does not need to filter classes by annotation; that is handled separately.
     */
    Set<Class<?>> getClassesForScanning();
}
