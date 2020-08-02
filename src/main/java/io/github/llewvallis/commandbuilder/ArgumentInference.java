package io.github.llewvallis.commandbuilder;

import lombok.experimental.UtilityClass;
import lombok.extern.java.Log;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Optional;

@Log
@UtilityClass
/* package-private */ class ArgumentInference {

    public void infer(CommandBuilder builder, Object instance, DefaultInferenceProvider defaultInferenceProvider) {
        Class<?> cls = instance.getClass();

        Method method = getMethodByAnnotation(ExecuteCommand.class, cls, cls);
        Parameter[] parameters = method.getParameters();

        for (int i = 1; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            ArgumentParser<?> parser = getParserForParameter(instance, method, parameter, defaultInferenceProvider);

            if (parameter.isVarArgs()) {
                builder.variadicArgument(parser);
            } else {
                builder.argument(parser);
            }
        }
    }

    public Method getMethodByAnnotation(Class<? extends Annotation> annotation,
                                                              Class<?> currentTarget, Class<?> originalTarget) {
        Method result = null;

        for (Method method : currentTarget.getDeclaredMethods()) {
            if (method.isAnnotationPresent(annotation)) {
                if (result == null) {
                    result = method;
                } else {
                    throw new ReflectionCommandCallbackException("multiple methods in class " + currentTarget +
                            " were annotated with " + annotation);
                }
            }
        }

        if (result == null) {
            Class<?> parent = currentTarget.getSuperclass();

            if (parent == null) {
                throw new ReflectionCommandCallbackException("no methods in class " + originalTarget + " were annotated with " +
                        annotation);
            } else {
                return getMethodByAnnotation(annotation, parent, originalTarget);
            }
        }

        return result;
    }

    public AccessibleObject getFieldOrMethodByName(String name, Class<?> currentTarget, Class<?> originalTarget) {
        try {
            return currentTarget.getDeclaredMethod(name);
        } catch (NoSuchMethodException ignored) { }

        try {
            return currentTarget.getDeclaredField(name);
        } catch (NoSuchFieldException ignored) { }

        try {
            return currentTarget.getMethod(name);
        } catch (NoSuchMethodException ignored) { }

        try {
            return currentTarget.getField(name);
        } catch (NoSuchFieldException ignored) { }

        Class<?> parent = currentTarget.getSuperclass();
        if (parent == null) {
            throw new ReflectionCommandCallbackException("no zero argument methods or fields in " + originalTarget +
                    " were named " + name);
        } else {
            return getFieldOrMethodByName(name, parent, originalTarget);
        }
    }

    private ArgumentParser<?> getParserForParameter(Object instance, Method method, Parameter parameter,
                                                    DefaultInferenceProvider defaultInferenceProvider) {
        Optional<? extends ArgumentParser<?>> defaultParser = defaultInferenceProvider.getForType(parameter.getType());
        ArgumentParser<?> previousParser = null;

        for (Annotation annotation : parameter.getDeclaredAnnotations()) {
            ParserAnnotation metaAnnotation = annotation.annotationType().getAnnotation(ParserAnnotation.class);

            if (metaAnnotation != null) {
                if (metaAnnotation.transformsPrevious() && previousParser == null) {
                    previousParser = defaultParser.orElseThrow(() -> new ReflectionCommandCallbackException(
                            parameter + " had a transforming annotation as its first inference annotation and no " +
                                    "default inference was available"));
                }

                if (!metaAnnotation.transformsPrevious() && previousParser != null) {
                    throw new ReflectionCommandCallbackException(parameter + " has a non-transforming annotation " +
                            "after a previous argument inference annotation");
                }

                ArgumentInferenceContext<?> ctx = new ArgumentInferenceContext<>(instance, method, parameter, annotation);
                previousParser = getParserFromAnnotation(ctx, previousParser);
            }
        }

        if (previousParser == null) {
            return defaultParser.orElseThrow(() -> new ReflectionCommandCallbackException(
                    parameter + " had no inference annotations and no default inference was available"));
        }

        return previousParser;
    }

    private ArgumentParser<?> getParserFromAnnotation(ArgumentInferenceContext<?> ctx, ArgumentParser<?> previous) {
        ParserAnnotation metaAnnotation = ctx.getAnnotation().annotationType().getAnnotation(ParserAnnotation.class);

        Class<?> factoryClass = metaAnnotation.value();
        String factoryMethodName = metaAnnotation.factoryMethodName();
        Method factoryMethod = ReflectionUtil.getMethodByName(factoryMethodName, factoryClass, factoryClass);
        factoryMethod.setAccessible(true);

        if (!factoryMethod.isAnnotationPresent(ArgumentInferenceFactory.class)) {
            log.warning("Argument parser factory method " + factoryMethod + " was not annotated with " + ArgumentInferenceFactory.class);
        }

        Object[] invocationArgs;
        if (metaAnnotation.transformsPrevious()) {
            invocationArgs = new Object[] { ctx, previous };
        } else {
            invocationArgs = new Object[] { ctx };
        }

        try {
            ArgumentParser<?> parser = (ArgumentParser<?>) factoryMethod.invoke(null, invocationArgs);
            if (parser == null) {
                throw new ReflectionCommandCallbackException("got null from " + factoryMethod);
            }

            return parser;
        } catch (InvocationTargetException e) {
            throw new ReflectionCommandCallbackException("method " + factoryMethod + " threw an unhandled exception", e.getCause());
        } catch (IllegalArgumentException e) {
            throw new ReflectionCommandCallbackException("method " + factoryMethod + " did not have the expected signature");
        } catch (NullPointerException e) {
            throw new ReflectionCommandCallbackException("method " + factoryMethod + " was not static");
        } catch (ClassCastException e) {
            throw new ReflectionCommandCallbackException("method " + factoryMethod + " did not return an argument parser");
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
