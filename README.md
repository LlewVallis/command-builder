# Command builder

A framework for constructing Bukkit commands.

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

## Composite commands and subcommands

```java
import io.github.llewvallis.commandbuilder.*;
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

        // Setup the command builder without building it. By default a reflection command callback is used
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

        @Override
        protected void configure(CommandBuilder builder) {
            builder.argument(new StringArgument());
        }

        @ExecuteCommand
        private void execute(CommandContext ctx, String person) {
            ctx.getSender().sendMessage("Howdy " + person);
        }
    }
}
```
