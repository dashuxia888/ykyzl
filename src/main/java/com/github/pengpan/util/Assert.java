package com.github.pengpan.util;

public class Assert {

    public static void isTrue(boolean bool) {
        if (!bool) {
            throw new RuntimeException();
        }
    }

    public static void isTrue(boolean bool, String format, Object... args) {
        if (!bool) {
            throw new RuntimeException(String.format(format, args));
        }
    }

    public static void notEmpty(String str) {
        if (StringUtil.isEmpty(str)) {
            throw new NullPointerException();
        }
    }

    public static void notEmpty(String str, String format, Object... args) {
        if (StringUtil.isEmpty(str)) {
            throw new NullPointerException(String.format(format, args));
        }
    }

    public static void notNull(Object object) {
        notNull(object, null);
    }

    public static void notNull(Object object, String format, Object... args) {
        if (object == null) {
            throw new NullPointerException(String.format(format, args));
        }
    }

    public static void illegalNull(Object object, String format, Object... args) {
        notNull(object, format, args);
    }

    public static void isLegalId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Illegal ID");
        }
    }
}
