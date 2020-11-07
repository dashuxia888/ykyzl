package com.github.pengpan.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {

    public static String md5(String str) {
        if (str == null) {
            return StringUtil.EMPTY;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            return new BigInteger(1, md.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5加密出现错误");
        }
    }
}
