package io.github.llewvallis.commandbuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Automatically builds commands and registers them by scanning annotated classes.
 *
 * @see CommandBuilder
 */
@Log
@RequiredArgsConstructor
public class AutoCommandBuilder {

    private final Plugin plugin;
    private final Set<AutoCommandSource> sources = new HashSet<>();

    private Set<Class<?>> classes = null;
    private final Map<Class<?>, Set<Class<?>>> subCommandMap = new HashMap<>();
    private boolean isUsed = false;

    /**
     * Add a source of classes for scanning.
     */
    public AutoCommandBuilder source(AutoCommandSource source) {
        assertNotUsed();
        sources.add(source);
        return this;
    }

    /**
     * Use a JAR file as a source of classes for scanning.
     *
     * This is most commonly used in conjunction with {@link JavaPlugin#getFile()}.
     */
    public AutoCommandBuilder jarSource(File file) {
        return source(new JarAutoCommandSource(file));
    }

    /**
     * Scan all of the sources registered through {@link #source(AutoCommandSource)} and register any eligible commands.
     */
    public void register() {
        assertNotUsed();

        Set<Class<?>> subCommands = getClassesWithAnnotation(AutoSubCommand.class);
        subCommands.forEach(this::registerSubCommand);

        Set<Class<?>> topLevelCommands = getClassesWithAnnotation(AutoCommand.class);
        for (Class<?> commandClass : topLevelCommands) {
            try {
                registerTopLevelCommand(commandClass);
            } catch (ReflectionCommandCallbackException e) {
                log.log(Level.SEVERE, "Failed to register auto command " + commandClass, e);
            }
        }

        for (Map.Entry<Class<?>, Set<Class<?>>> entry : subCommandMap.entrySet()) {
            for (Class<?> unusedSubCommand : entry.getValue()) {
                log.warning(unusedSubCommand + " was never created since " + entry.getKey() + " wasn't");
            }
        }

        isUsed = true;
    }

    private Set<Class<?>> getClassesWithAnnotation(Class<? extends Annotation> annotation) {
        if (classes == null) {
            classes = new HashSet<>();
            for (AutoCommandSource source : sources) {
                classes.addAll(source.getClassesForScanning());
            }
        }

        return classes.stream()
                .filter(cls -> cls.isAnnotationPresent(annotation))
                .collect(Collectors.toSet());
    }

    private void registerTopLevelCommand(Class<?> commandClass) {
        AutoCommand annotation = commandClass.getAnnotation(AutoCommand.class);
        Object instance = createInstance(commandClass, annotation.factoryMethod());

        if (instance instanceof TopLevelCommand) {
            TopLevelCommand command = (TopLevelCommand) instance;
            registerExecutor(command.getName(), command.getOrCreateExecutor(), false);
        } else if (instance instanceof CompositeTopLevelCommand) {
            CompositeTopLevelCommand oldCommand = (CompositeTopLevelCommand) instance;

            CompositeTopLevelCommand newCommand = new CompositeTopLevelCommand() {
                @Override
                protected void configure(CompositeCommandBuilder builder) {
                    builder.metadata(plugin.getDescription(), getName());

                    oldCommand.configure(builder);

                    getAutoSubCommandsForClass(commandClass, builder)
                            .forEach(builder::command);
                }

                @Override
                public String getName() {
                    return oldCommand.getName();
                }
            };

            registerExecutor(newCommand.getName(), newCommand.getOrCreateExecutor(), true);
        } else {
            throw new ReflectionCommandCallbackException("auto command " + instance + " did not have an " +
                    "appropriate superclass");
        }
    }

    private void registerSubCommand(Class<?> commandClass) {
        AutoSubCommand annotation = commandClass.getAnnotation(AutoSubCommand.class);
        subCommandMap
                .computeIfAbsent(annotation.value(), k -> new HashSet<>())
                .add(commandClass);
    }

    private Set<SubCommand> getAutoSubCommandsForClass(Class<?> cls, CompositeCommandBuilder builder) {
        Set<Class<?>> subCommandClasses = subCommandMap.remove(cls);
        if (subCommandClasses == null) {
            return Set.of();
        }

        Set<SubCommand> subCommands = new HashSet<>();
        for (Class<?> subCommandClass : subCommandClasses) {
            try {
                SubCommand subCommand = createSubCommand(subCommandClass, builder);
                subCommands.add(subCommand);
            } catch (ReflectionCommandCallbackException e) {
                log.log(Level.SEVERE, "Failed to register auto subcommand " + subCommandClass, e);
            }
        }

        return subCommands;
    }

    private SubCommand createSubCommand(Class<?> subCommandClass, CompositeCommandBuilder builder) {
        AutoSubCommand annotation = subCommandClass.getAnnotation(AutoSubCommand.class);
        Object instance = createInstance(subCommandClass, annotation.factoryMethod());

        if (instance instanceof SubCommand) {
            return (SubCommand) instance;
        } else if (instance instanceof CompositeSubCommand) {
            CompositeSubCommand compositeSubCommand = (CompositeSubCommand) instance;

            return new CompositeSubCommandImpl(builder, new CompositeSubCommand() {
                @Override
                protected void configure(CompositeCommandBuilder builder) {
                    compositeSubCommand.configure(builder);

                    getAutoSubCommandsForClass(subCommandClass, builder)
                            .forEach(builder::command);
                }

                @Override
                public String getName() {
                    return compositeSubCommand.getName();
                }

                @Override
                public String getDescription() {
                    return compositeSubCommand.getDescription();
                }

                @Override
                public Optional<String> getPermission() {
                    return compositeSubCommand.getPermission();
                }
            });
        } else {
            throw new ReflectionCommandCallbackException("auto subcommand " + instance + " did not have an " +
                    "appropriate superclass");
        }
    }

    private void registerExecutor(String name, TabExecutor executor, boolean injectUsage) {
        PluginCommand bukkitCommand = Bukkit.getServer().getPluginCommand(name);
        if (bukkitCommand == null) {
            throw new ReflectionCommandCallbackException("command " + name + " was not found");
        }

        bukkitCommand.setExecutor(executor);
        bukkitCommand.setTabCompleter(executor);

        if (injectUsage && bukkitCommand.getUsage().isBlank()) {
            bukkitCommand.setUsage(CompositeCommandBuilder.getDefaultUsage(name));
        }
    }

    private Object createInstance(Class<?> commandClass, String factoryMethodName) {
        if (factoryMethodName.equals("")) {
            try {
                Constructor constructor = commandClass.getDeclaredConstructor();
                constructor.setAccessible(true);

                return constructor.newInstance();
            } catch (InstantiationException e) {
                throw new ReflectionCommandCallbackException(commandClass + " was abstract");
            } catch (InvocationTargetException e) {
                throw new ReflectionCommandCallbackException("constructor on " + commandClass + " threw an unhandled " +
                        "exception", e.getCause());
            } catch (NoSuchMethodException e) {
                throw new ReflectionCommandCallbackException("no zero argument constructor on " + commandClass);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            Method factoryMethod = ReflectionUtil.getMethodByName(factoryMethodName, commandClass, commandClass);
            factoryMethod.setAccessible(true);

            if (!factoryMethod.isAnnotationPresent(AutoCommandFactory.class)) {
                log.warning("Auto command factory method " + factoryMethod + " was not annotated with " + AutoCommandFactory.class);
            }

            AutoCommandContext ctx = new AutoCommandContext(plugin);

            try {
                return factoryMethod.invoke(null, ctx);
            } catch (InvocationTargetException e) {
                throw new ReflectionCommandCallbackException("method " + factoryMethod + " threw an unhandled exception", e.getCause());
            } catch (IllegalArgumentException e) {
                throw new ReflectionCommandCallbackException("method " + factoryMethod + " did not have the expected signature");
            } catch (NullPointerException e) {
                throw new ReflectionCommandCallbackException("method " + factoryMethod + " was not static");
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void assertNotUsed() {
        if (isUsed) {
            throw new IllegalStateException("already used");
        }
    }

}
