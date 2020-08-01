package io.github.llewvallis.commandbuilder;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Context passed to an argument inference factory.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ArgumentInferenceContext<T extends Annotation> {

    private final Object instance;
    private final Method method;
    private final Parameter parameter;
    private final T annotation;
}
