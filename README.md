# Command builder

A framework for constructing Bukkit commands.

 [![Maven Central](https://img.shields.io/maven-central/v/io.github.llewvallis/command-builder.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.llewvallis%22%20AND%20a:%22command-builder%22)
 [![Javadoc](https://javadoc.io/badge2/io.github.llewvallis/command-builder/javadoc.svg)](https://javadoc.io/doc/io.github.llewvallis/command-builder)

## Examples
### Creating a command

```java
import io.github.llewvallis.commandbuilder.ArgumentParseException;
import io.github.llewvallis.commandbuilder.CommandBuilder;
import io.github.llewvallis.commandbuilder.CommandContext;
import io.github.llewvallis.commandbuilder.ExecuteCommand;
import io.github.llewvallis.commandbuilder.ReflectionCommandCallback;
import io.github.llewvallis.commandbuilder.arguments.IntegerArgument;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public class Plugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Creates a command which displays ten times the argument provided. The argument must be an integer which is
        // greater than 0 and must not be 42
        new CommandBuilder()
                .argument(
                        new IntegerArgument()
                                // Denies any number below 1
                                .withMin(1)
                                // Sets 1, 2 and 3 as tab completions
                                .usingCompletions(() -> Set.of("1", "2", "3"))
                                // Multiplies the resulting value by 10, unless its 42, in which case an error message
                                // is displayed
                                .map(value -> {
                                    if (value == 42) {
                                        throw new ArgumentParseException("cannot use 42");
                                    } else {
                                        return value * 10;
                                    }
                                })
                )
                // Binds the builder to "ten-times". The one argument build method can also be used to just generate a
                // TabExecutor
                .build(new ReflectionCommandCallback(this), getCommand("ten-times"));
    }

    // Executed when the command is successfully parsed
    @ExecuteCommand
    private void execute(CommandContext ctx, int number) {
        ctx.sender.sendMessage("Received " + number);
    }
}
```

## Optional arguments

```java
import io.github.llewvallis.commandbuilder.CommandBuilder;
import io.github.llewvallis.commandbuilder.CommandContext;
import io.github.llewvallis.commandbuilder.ExecuteCommand;
import io.github.llewvallis.commandbuilder.ReflectionCommandCallback;
import io.github.llewvallis.commandbuilder.arguments.StringArgument;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class Plugin extends JavaPlugin {

    @Override
    public void onEnable() {
        new CommandBuilder()
                .argument(new StringArgument()
                    // Mark the argument as optional
                    .optional())
                .build(new ReflectionCommandCallback(this), getCommand("greet"));
    }

    @ExecuteCommand
    private void execute(CommandContext ctx, String person) {
        ctx.sender.sendMessage("Hello " + Objects.requireNonNullElse(person, "world") + "!");
    }
}

```

## Variadic arguments

```java
import io.github.llewvallis.commandbuilder.CommandBuilder;
import io.github.llewvallis.commandbuilder.CommandContext;
import io.github.llewvallis.commandbuilder.ExecuteCommand;
import io.github.llewvallis.commandbuilder.ReflectionCommandCallback;
import io.github.llewvallis.commandbuilder.arguments.StringArgument;
import org.bukkit.plugin.java.JavaPlugin;

public class Plugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Creates a command which echos the command's arguments back to the sender
        new CommandBuilder()
                .variadicArgument(new StringArgument())
                .build(new ReflectionCommandCallback(this), getCommand("echo"));
    }

    @ExecuteCommand
    private void execute(CommandContext ctx, String... arguments) {
        ctx.sender.sendMessage(String.join(" ", arguments));
    }
}
```

## Argument inference

```java
import io.github.llewvallis.commandbuilder.Arg;
import io.github.llewvallis.commandbuilder.ArgumentParser;
import io.github.llewvallis.commandbuilder.CommandBuilder;
import io.github.llewvallis.commandbuilder.CommandContext;
import io.github.llewvallis.commandbuilder.ExecuteCommand;
import io.github.llewvallis.commandbuilder.ReflectionCommandCallback;
import io.github.llewvallis.commandbuilder.arguments.IntegerArgument;
import io.github.llewvallis.commandbuilder.arguments.StringArgument;
import io.github.llewvallis.commandbuilder.arguments.StringSetArgument;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Plugin extends JavaPlugin {

    @Override
    public void onEnable() {
        new CommandBuilder()
            // Add arguments based on method annotations
            .infer(this)
            .build(new ReflectionCommandCallback(this), getCommand("print-powers-of-two"));
    }

    // Create an argument as a field to be reference from an annotation. Zero argument methods also work
    private ArgumentParser<Integer> customArg = new IntegerArgument().map(x -> (int) Math.pow(2, x));

    @ExecuteCommand
    private void execute(
            CommandContext ctx,
            // Calls the zero argument constructor of StringArgument and then calls optional() on it
            @Arg(value = StringArgument.class, optional = true) String label,
            // Uses StringSetArgument's custom Arg annotation to generate an argument
            @StringSetArgument.Arg({ ",", ":", ";", "-" }) String separator,
            // Fetch the argument from the customArg field
            @Arg(member = "customArg") int... numbers
    ) {
        String numberString = IntStream.of(numbers)
                .mapToObj(Integer::toString)
                .collect(Collectors.joining(separator));

        ctx.getSender().sendMessage(label + " = " + numberString);
    }
}
```

## Composite commands and subcommands

```java
import io.github.llewvallis.commandbuilder.Arg;
import io.github.llewvallis.commandbuilder.CommandBuilder;
import io.github.llewvallis.commandbuilder.CommandContext;
import io.github.llewvallis.commandbuilder.CompositeCommandBuilder;
import io.github.llewvallis.commandbuilder.ExecuteCommand;
import io.github.llewvallis.commandbuilder.SubCommand;
import io.github.llewvallis.commandbuilder.arguments.StringArgument;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public class Plugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Creates a composite command which can be used as `greet <hello|howdy|help> [args...]`
        new CompositeCommandBuilder()
                // Register the subcommands. A help command is automatically registered
                .command(new Hello())
                .command(new Howdy())
                // The zero argument build method can be used to just get a TabExecutor
                .build(getCommand("greet"));
    }

    private static class Hello extends SubCommand {

        // The label used to call the command
        @Override
        public String getName() {
            return "hello";
        }

        // A description detailing how the command works etc. for the help menu
        @Override
        public String getDescription() {
            return "Says hello";
        }

        // The message used in the help menu and the command error message
        @Override
        public String getUsageMessage() {
            return "hello <person>";
        }

        // Setup the command builder without building it. By default a reflection command callback is used when 
        // constructing
        @Override
        protected void configure(CommandBuilder builder) {
            builder.argument(new StringArgument());
        }

        @ExecuteCommand
        private void execute(CommandContext ctx, String person) {
            ctx.getSender().sendMessage("Hello " + person);
        }
    }

    private static class Howdy extends SubCommand {

        @Override
        public String getName() {
            return "howdy";
        }

        @Override
        public String getDescription() {
            return "Say howdy";
        }

        @Override
        public String getUsageMessage() {
            return "howdy <person>";
        }

        // Require that only people with the `greet.howdy` permission can access the command
        @Override
        public Optional<String> getPermission() {
            return Optional.of("greet.howdy");
        }

        @ExecuteCommand
        private void execute(
                CommandContext ctx,
                // Use annotation based argument inference. Also works for normal commands by calling the
                // CommandBuilder.infer method
                @Arg(StringArgument.class) String person
        ) {
            ctx.getSender().sendMessage("Howdy " + person);
        }
    }
}
```
