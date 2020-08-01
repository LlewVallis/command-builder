package io.github.llewvallis.commandbuilder;

import io.github.llewvallis.commandbuilder.arguments.OptionalOverrideArgument;
import lombok.experimental.UtilityClass;

import java.lang.reflect.*;

@UtilityClass
/* package-private */ class ArgumentInferers {

    @ArgumentInferenceFactory
    private ArgumentParser<?> createParserFromClassArg(ArgumentInferenceContext<ClassArg> ctx) {
        Class<?> parserClass = ctx.getAnnotation().value();

        try {
            Constructor constructor = parserClass.getDeclaredConstructor();
            constructor.setAccessible(true);

            return (ArgumentParser<?>) constructor.newInstance();
        } catch (InstantiationException e) {
            throw new ReflectionCommandCallbackException(parserClass + " was abstract");
        } catch (InvocationTargetException e) {
            throw new ReflectionCommandCallbackException("constructor on " + parserClass + " threw an unhandled " +
                    "exception", e.getCause());
        } catch (NoSuchMethodException e) {
            throw new ReflectionCommandCallbackException("no zero argument constructor on " + parserClass);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @ArgumentInferenceFactory
    private ArgumentParser<?> createParserFromMemberArg(ArgumentInferenceContext<MemberArg> ctx) {
        String parserMember = ctx.getAnnotation().value();
        Object instance = ctx.getInstance();
        Class<?> cls = instance.getClass();

        AccessibleObject fieldOrMethod = ArgumentInference.getFieldOrMethodByName(parserMember, cls, cls);
        fieldOrMethod.setAccessible(true);

        Object value;
        try {
            if (fieldOrMethod instanceof Field) {
                value = ((Field) fieldOrMethod).get(instance);
            } else {
                value = ((Method) fieldOrMethod).invoke(instance);
            }
        } catch (InvocationTargetException e) {
            throw new ReflectionCommandCallbackException("method " + fieldOrMethod + " threw an unhandled exception", e.getCause());
        } catch (IllegalAccessException e){
            throw new RuntimeException(e);
        }

        try {
            return (ArgumentParser<?>) value;
        } catch (ClassCastException e) {
            throw new ReflectionCommandCallbackException("got " + value + " from " + fieldOrMethod + " but expected" +
                    " an argument parser");
        }
    }

    @ArgumentInferenceFactory
    private ArgumentParser<?> createParserFromOptionalArg(ArgumentInferenceContext<OptionalArg> ctx, ArgumentParser<?> previous) {
        return new OptionalOverrideArgument<>(previous, ctx.getAnnotation().optional());
    }
}
