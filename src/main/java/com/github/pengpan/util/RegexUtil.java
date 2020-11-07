package com.github.pengpan.util;

public class RegexUtil {

    public static boolean isMobile(String mobile) {
        if (StringUtil.isEmpty(mobile)) {
            return false;
        }
        return mobile.matches("^1[3456789]{1}[0-9]{9}$");
    }
}
