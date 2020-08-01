package io.github.llewvallis.commandbuilder;

import lombok.experimental.UtilityClass;

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
}
