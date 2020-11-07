package com.github.pengpan.util;

import java.util.UUID;

public class StringUtil {

    public final static String EMPTY = "";

    public static boolean isEmpty(String str) {
        return str == null || str.equals(EMPTY);
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean isBlank(String str) {
        int strLen;
        if (str != null && (strLen = str.length()) != 0) {
            for (int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(str.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    public static String uuid(boolean upperCase) {
        return upperCase ? uuid().toUpperCase() : uuid();
    }
}
