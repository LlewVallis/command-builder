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

    /**
     * The instance which triggered argument inference.
     */
    private final Object instance;

    /**
     * The method whose parameters are being used for inference.
     */
    private final Method method;

    /**
     * The parameter currently being used to infer an argument.
     */
    private final Parameter parameter;

    /**
     * The annotation currently being processed to infer an argument.
     */
    private final T annotation;
}
