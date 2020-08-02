package io.github.llewvallis.commandbuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as eligible for auto subcommand registration.
 *
 * Auto subcommands differ from autocommands in that they are not registered as Bukkit commands, but are instead
 * automatically added as subcommands to instances of {@link CompositeTopLevelCommand} or {@link CompositeSubCommand}.
 * Auto subcommands are only registered to other auto commands or auto subcommands. A class annotated with this should
 * inherit either {@link SubCommand} or {@link CompositeSubCommand}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoSubCommand {

    /**
     * The class of the {@link CompositeTopLevelCommand} or {@link CompositeSubCommand} to attach to.
     */
    Class<?> value();

    /**
     * The factory method used to instantiate the subcommand.
     *
     * If this is an empty string, a zero argument constructor will be used to instantiate the subcommand. Otherwise,
     * this should point to a static method in the annotated class which returns a {@link SubCommand} or
     * {@link CompositeSubCommand} and takes a {@link AutoCommandContext} as its sole parameter.
     */
    String factoryMethod() default "";
}
