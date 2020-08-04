# Command Builder

[![Maven Central](https://img.shields.io/maven-central/v/io.github.llewvallis/command-builder.svg?label=central)](https://search.maven.org/search?q=g:%22io.github.llewvallis%22%20AND%20a:%22command-builder%22)
[![Javadoc](https://javadoc.io/badge2/io.github.llewvallis/command-builder/javadoc.svg)](https://javadoc.io/doc/io.github.llewvallis/command-builder)

Command builder is a framework for constructing Bukkit commands. At its core, it allows you to write the essence of your
command without having to worry about parsing arguments and plumbing things together. Here's a quick example that 
creates a calculator command for adding or subtracting two numbers:

```java
public class MyPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        new CommandBuilder()
                .infer(this)
                .build(new ReflectionCommandCallback(this), getCommand("my-command"));
    }

    @ExecuteCommand
    private void execute(CommandContext ctx, 
                         @StringSetArgument.Arg({ "add", "sub" }) String operation,
                         int a,
                         int b) {
        int result;
        if (operation.equals("add")) {
            result = a + b;
        } else {
            result = a - b;
        }

        ctx.getSender().sendMessage("The result is: " + result);
    }
}
```

Command builder also lets you define your own types of arguments and has other powerful features to make creating 
commands easy. Feel free to contact `LlewVallis#5734` on Discord if there is any thing I can help with.

## Installation

Add the library you your own plugin using Maven:

```xml
<dependency>
    <groupId>io.github.llewvallis</groupId>
    <artifactId>command-builder</artifactId>
    <version>2.2.0</version>
</dependency>
```

It is recommended to [shade](https://maven.apache.org/plugins/maven-shade-plugin) this dependency to avoid clashes with
other plugins that use command builder.

## Concepts
### Command builders

Command builders are the central concept to the library. They allow you to generate a command (`TabExecutor`) by 
specifying a list of `ArgumentParser`s and a `CommandCallback`. The generated command will then use all of its argument 
parsers to process the command and will tell the callback the result. Manually adding argument parsers and manually 
writing a command callback for each command can be cumbersome, though. For this reason you can use argument inference 
and `ReflectionCommandCallback`.

### Argument parsers

Argument parsers are able to convert a single command argument from a string into some other type. For example, an
`IntegerArgument` can parse a string into an integer. Arguments can also be marked as optional, in which case they will
default to `null` if not given when running the command.

### `ReflectionCommandCallback`

This is a special type of command callbacks which calls a method if the command parsed successfully and shows the user
an error message if it did not. `ReflectionCommandCallback` looks for any method annotated with `@ExecuteCommand` on the
object passed to its constructor. Custom command callbacks other than `ReflectionCommandCallback` can also be used, but 
that is rarely a good idea.

### Argument inference

Instead of manually adding arguments to a command, the `infer` method can be called on a command builder to make it 
automatically figure out the arguments that the command should accept. Argument inference works by looking for a method
using the same criteria as `ReflectionCommandCallback` and inspecting its parameters. Special annotations can also be 
added to parameters to nudge the argument inference in the right direction.

### Auto commands

Even with argument inference and `ReflectionCommandCallback`, it can be cumbersome to setup and register all the 
commands for a plugin. An `AutoCommandBuilder` can be used to scan all the classes in a package and register any auto
commands contained within. This is best illustrated with an example:

```java
// Put this where you'd like to register your commands. Probably in your onEnable
new AutoCommandBuilder(this)
        .jarSource(getFile(), "^my.plugin.commands.")
        .register();
```

And in a different file:

```java
package my.plugin.commands;

@AutoCommand
public class MyAutoCommand extends TopLevelCommand {

    @Override
    public String getName() {
        return "my-command";
    }

    @ExecuteCommand
    private void execute(CommandContext ctx,
                         @StringSetArgument.Arg({ "add", "sub" }) String operation,
                         int a,
                         int b) {
        int result;
        if (operation.equals("add")) {
            result = a + b;
        } else {
            result = a - b;
        }

        ctx.getSender().sendMessage("The result is: " + result);
    }
}
```

You can then continue to add as many auto commands as you like in the `my.plugin.commands` package and they will all be
configured and registered using argument inference and `ReflectionCommandCallback`.

### Composite commands

Sometimes its useful to have subcommands grouped together under an actual top level command. Command builder allows you
to do this easily using `CompositeCommandBuilder`. Here is another example:

```java
new CompositeCommandBuilder()
        .command(new SubCommand() {
            @Override
            public String getName() {
                return "hello";
            }

            @Override
            public String getDescription() {
                return "Says hello";
            }

            @Override
            public String getUsageMessage() {
                return "hello <name>";
            }

            @ExecuteCommand
            private void execute(CommandContext ctx, String name) {
                ctx.getSender().sendMessage("Hello " + name);
            }
        })
        .command(new SubCommand() {
            @Override
            public String getName() {
                return "howdy";
            }

            @Override
            public String getDescription() {
                return "Says howdy";
            }

            @Override
            public String getUsageMessage() {
                return "howdy <name>";
            }

            @ExecuteCommand
            private void execute(CommandContext ctx, String name) {
                ctx.getSender().sendMessage("Howdy " + name);
            }
        })
        .build(getCommand("my-command"));
```

This will also generate you a `help` subcommand which displays a listing of all the other subcommands. As you can see, 
it is quite inconvenient to specify descriptions and usage messages directly in the code. To avoid doing this we can 
instruct the `CompositeCommandBuilder` to read your plugin's `plugin.yml` file. Here's an example:

```java
new CompositeCommandBuilder()
        .metadata(getDescription(), "my-command")
        .command(new SubCommand() {
            @Override
            public String getName() {
                return "hello";
            }

            @ExecuteCommand
            private void execute(CommandContext ctx, String name) {
                ctx.getSender().sendMessage("Hello " + name);
            }
        })
        .command(new SubCommand() {
            @Override
            public String getName() {
                return "howdy";
            }

            @ExecuteCommand
            private void execute(CommandContext ctx, String name) {
                ctx.getSender().sendMessage("Howdy " + name);
            }
        })
        .build(getCommand("my-command"));
```

And the `spigot.yml`:

```yaml
commands:
  my-command:
    description: Description for my-command
    subcommands:
      hello:
        description: Description for hello
        usage: hello <name>
      howdy:
        description: Description for howdy
        usage: howdy <name>
```

Composite commands also support auto registration as shown below:

```java
@AutoCommand
public class MyCompositeCommand extends CompositeTopLevelCommand {

    @Override
    public String getName() {
        return "my-command";
    }
}
```

And in a different file:

```java
@AutoSubCommand(MyCompositeCommand.class)
public class HelloCommand extends SubCommand {

    @Override
    public String getName() {
        return "hello";
    }

    @ExecuteCommand
    private void execute(CommandContext ctx, String name) {
        ctx.getSender().sendMessage("Hello " + name);
    }
}
```

Auto registration of composite commands will automatically read from your `plugin.yml` if necessary.

Composite commands can also be nested within eachother using the `CompositeSubCommand` class. Auto registration also 
works in this case.
