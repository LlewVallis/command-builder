package io.github.llewvallis.commandbuilder;

import io.github.llewvallis.commandbuilder.arguments.FloatArgument;
import io.github.llewvallis.commandbuilder.arguments.IntegerArgument;
import io.github.llewvallis.commandbuilder.arguments.StringArgument;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Provides default {@link ArgumentParser}s for command arguments that don't have a parser explicitly set via inference
 * annotations.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DefaultInferenceProvider {

    /**
     * A global instance of this class that new {@link CommandBuilder}s inherit.
     */
    @Getter
    private static final DefaultInferenceProvider global = new DefaultInferenceProvider();

    static {
        global
                .register(String.class, new StringArgument())
                .register(float.class, new FloatArgument())
                .register(double.class, new FloatArgument().map(Float::doubleValue))
                .register(int.class, new IntegerArgument());
    }

    private Map<Class<?>, ArgumentParser<?>> parsers = new HashMap<>();

    /**
     * Associate a class with an {@link ArgumentParser}.
     *
     * All parents of the provided class that don't have an association will also be associated with the parser.
     */
    public DefaultInferenceProvider register(Class<?> cls, ArgumentParser<?> parser) {
        if (cls == void.class) {
            throw new IllegalArgumentException("cannot register void");
        }

        parsers.put(cls, parser);

        for (
                Class<?> parent = cls.getSuperclass();
                parent != null && !parsers.containsKey(parent);
                parent = parent.getSuperclass()
        ) {
            parsers.put(parent, parser);
        }

        if (cls.isPrimitive()) {
            register(ReflectionUtil.boxedType(cls), parser);
        }

        return this;
    }

    /* package-private */ <T> Optional<ArgumentParser<T>> getForType(Class<T> cls) {
        @SuppressWarnings("unchecked")
        Optional<ArgumentParser<T>> result = Optional.ofNullable((ArgumentParser<T>) parsers.get(cls));
        return result;
    }

    /* package-private */ DefaultInferenceProvider fork() {
        DefaultInferenceProvider result = new DefaultInferenceProvider();
        result.parsers = new HashMap<>(parsers);
        return result;
    }
}
