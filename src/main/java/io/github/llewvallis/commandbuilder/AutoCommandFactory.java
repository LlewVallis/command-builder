package io.github.llewvallis.commandbuilder;

import java.lang.annotation.*;

/**
 * Marks a method as being used in conjunction with {@link AutoCommand} as a way to create {@link TopLevelCommand}s and
 * {@link CompositeTopLevelCommand}s.
 *
 * This annotation has no effect, but it is recommended as a form of documentation.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AutoCommandFactory { }
