package io.github.llewvallis.commandbuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If used on a method invoked by {@link ReflectionCommandCallback}, an error message will be displayed unless
 * {@link CommandContext#sender} is an instance of {@link org.bukkit.entity.Player}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PlayerOnlyCommand { }
