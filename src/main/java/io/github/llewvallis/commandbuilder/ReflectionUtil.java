package io.github.llewvallis.commandbuilder;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
/* package-private */ class ReflectionUtil {

    public Class<?> boxedType(Class<?> type) {
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

    /* package-private */ Method getMethodByName(String name, Class<?> currentTarget, Class<?> originalTarget) {
        List<Method> methodCandidates = new ArrayList<>();
        methodCandidates.addAll(List.of(currentTarget.getDeclaredMethods()));
        methodCandidates.addAll(List.of(currentTarget.getMethods()));

        for (Method method : methodCandidates) {
            if (method.getName().equals(name)) {
                return method;
            }
        }

        Class<?> parent = currentTarget.getSuperclass();
        if (parent == null) {
            throw new ReflectionCommandCallbackException("no method named " + name + " in class " + originalTarget);
        } else {
            return getMethodByName(name, parent, originalTarget);
        }
    }
}
