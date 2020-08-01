package io.github.llewvallis.commandbuilder;

import lombok.SneakyThrows;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * A {@link CommandCallback} which delegates to a method annotated with {@link ExecuteCommand} on a provided instance.
 */
public class ReflectionCommandCallback implements CommandCallback {

    private final Object instance;

    public ReflectionCommandCallback(Object instance) {
        this.instance = instance;
    }

    @Override
    public void onSuccess(List<Object> argumentValues, List<Object> variadicArgumentValues, CommandContext context) {
        List<Object> argumentValuesWithContext = new ArrayList<>();
        argumentValuesWithContext.add(context);
        argumentValuesWithContext.addAll(argumentValues);

        Method onSuccessMethod = ArgumentInference.getMethodByAnnotation(ExecuteCommand.class, instance.getClass(), instance.getClass());
        onSuccessMethod.setAccessible(true);
        runCallback(onSuccessMethod, argumentValuesWithContext, variadicArgumentValues, context);
    }

    @Override
    public void onFailure(CommandParseException cause, CommandContext context) {
        TextComponent errorMessage = new TextComponent("Incorrect command: " + cause.getMessage());
        errorMessage.setColor(ChatColor.RED);

        TextComponent usageMessage = new TextComponent("Usage: " + context.getUsageMessage());
        usageMessage.setItalic(true);
        usageMessage.setColor(ChatColor.RED);

        context.getSender().spigot().sendMessage(errorMessage);
        context.getSender().spigot().sendMessage(usageMessage);
    }

    @SneakyThrows({ IllegalAccessException.class })
    private void runCallback(Method callbackMethod, List<Object> argumentValues,
                                       List<Object> variadicArgumentValues, CommandContext context) {
        if (callbackMethod.isAnnotationPresent(PlayerOnlyCommand.class) && !(context.getSender() instanceof Player)) {
            TextComponent message = new TextComponent("Only players can use this command");
            message.setColor(ChatColor.RED);

            context.getSender().spigot().sendMessage(message);
            return;
        }

        checkCallback(callbackMethod, argumentValues, variadicArgumentValues);

        try {
            runCallbackUnchecked(callbackMethod, argumentValues, variadicArgumentValues);
        } catch (InvocationTargetException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Unhandled exception in command callback for " + context.getCommand(), e.getCause());
            throw new ReflectionCommandCallbackException("unhandled exception in callback method ", e.getCause());
        }
    }

    private void runCallbackUnchecked(Method callbackMethod, List<Object> argumentValues,
                                         List<Object> variadicArgumentValues)
            throws InvocationTargetException, IllegalAccessException {
        boolean variadic = variadicArgumentValues != null;
        int argumentCount = argumentValues.size() + (variadic ? 1 : 0) ;

        Object[] arguments = new Object[argumentCount];
        argumentValues.toArray(arguments);

        if (variadic) {
            Class<?>[] parameterTypes = callbackMethod.getParameterTypes();

            Class<?> variadicType = parameterTypes[parameterTypes.length - 1];
            Class<?> elementType = variadicType.getComponentType();

            Object[] untypedVariadicArguments = variadicArgumentValues.toArray();
            Object typedVariadicArguments = primitiveRespectingArrayCopy(untypedVariadicArguments, elementType);

            arguments[arguments.length - 1] = typedVariadicArguments;
        }

        callbackMethod.invoke(instance, arguments);
    }

    private Object primitiveRespectingArrayCopy(Object[] untypedVariadicArguments, Class<?> elementType) {
        Object array = Array.newInstance(elementType, untypedVariadicArguments.length);

        for (int i = 0; i < untypedVariadicArguments.length; i++) {
            Object argument = untypedVariadicArguments[i];
            Array.set(array, i, argument);
        }

        return array;
    }

    private void checkCallback(Method callbackMethod, List<Object> argumentValues,
                                   List<Object> variadicArgumentValues) {
        Class<?>[] argumentTypes = callbackMethod.getParameterTypes();
        boolean variadic = callbackMethod.isVarArgs();

        Class<?>[] nonVariadicArgumentTypes;
        Class<?> variadicArgumentType;

        if (variadic) {
            if (argumentTypes.length < 1) {
                throw new ReflectionCommandCallbackException("missing variadic argument in callback method");
            }

            nonVariadicArgumentTypes = new Class<?>[argumentTypes.length - 1];
            System.arraycopy(argumentTypes, 0, nonVariadicArgumentTypes, 0, argumentTypes.length - 1);

            variadicArgumentType = argumentTypes[argumentTypes.length - 1].getComponentType();
        } else {
            nonVariadicArgumentTypes = new Class<?>[argumentTypes.length];
            System.arraycopy(argumentTypes, 0, nonVariadicArgumentTypes, 0, argumentTypes.length);

            variadicArgumentType = null;
        }

        Class<?> returnType = callbackMethod.getReturnType();

        checkArguments(nonVariadicArgumentTypes, argumentValues);
        checkVariadicConsistency(variadicArgumentType, variadicArgumentValues);
        checkReturnType(returnType);
    }

    private static void checkArguments(Class<?>[] argumentTypes, List<Object> argumentValues) {
        if (argumentTypes.length != argumentValues.size()) {
            throw new ReflectionCommandCallbackException("method expected the wrong amount of arguments");
        }

        for (int i = 0; i < argumentTypes.length; i++) {
            Object value = argumentValues.get(i);
            Class<?> expectedType = argumentTypes[i];

            if (value == null) {
                if (expectedType.isPrimitive()) {
                    throw new ReflectionCommandCallbackException("primitive type " + expectedType + " cannot be "
                            + "optional, try using the boxed type instead");
                }
            } else {
                Class<?> valueType = value.getClass();

                if (!expectedType.isAssignableFrom(valueType) && !boxedType(expectedType).isAssignableFrom(valueType)) {
                    throw new ReflectionCommandCallbackException(valueType + " is not assignable to " + expectedType);
                }
            }
        }
    }

    private static Class<?> boxedType(Class<?> type) {
        if (type == int.class) {
            return Integer.class;
        } else if (type == byte.class) {
            return Byte.class;
        } else if (type == short.class) {
            return Short.class;
        } else if (type == long.class) {
            return Long.class;
        } else if (type == float.class) {
            return Float.class;
        } else if (type == double.class) {
            return Double.class;
        } else if (type == boolean.class) {
            return Boolean.class;
        } else if (type == char.class) {
            return Character.class;
        } else {
            return type;
        }
    }

    private void checkVariadicConsistency(Class<?> variadicArgumentType, List<Object> variadicArgumentValues) {
        if (variadicArgumentValues == null && variadicArgumentType != null) {
            throw new ReflectionCommandCallbackException("had a variadic argument for a non-variadic command");
        } else if (variadicArgumentValues != null && variadicArgumentType == null) {
            throw new ReflectionCommandCallbackException("missing variadic argument for variadic command");
        }

        if (variadicArgumentType != null) {
            for (Object value : variadicArgumentValues) {
                Class<?> valueType = value.getClass();

                if (!variadicArgumentType.isAssignableFrom(valueType) && !boxedType(variadicArgumentType).isAssignableFrom(valueType)) {
                    throw new ReflectionCommandCallbackException(value.getClass() + " is not assignable to " +
                            variadicArgumentType);
                }
            }
        }
    }

    private static void checkReturnType(Class<?> returnType) {
        if (returnType != Void.TYPE) {
            throw new ReflectionCommandCallbackException("return type must be void");
        }
    }
}
