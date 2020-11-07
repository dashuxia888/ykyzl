package com.github.pengpan.common.cache;

import com.github.pengpan.util.Assert;

public class KeyAssembly {

    private static final String PREFIX = "REDIS";

    public static String assemble(BaseKeyEnum keyEnum, String id, String... ids) {
        StringBuilder key = new StringBuilder(assemble(keyEnum, id));

        if (ids != null && ids.length > 0) {
            for (String i : ids) {
                key.append(":").append(i);
            }
        }
        return key.toString();
    }

    public static <T> String assemble(BaseKeyEnum keyEnum, T id) {
        Assert.illegalNull(id, "Null key");
        return PREFIX + ":" + keyEnum.module().name() + ":" + keyEnum.name() + ":" + id;
    }

    public static String assemble(BaseKeyEnum keyEnum) {
        return PREFIX + ":" + keyEnum.module().name() + ":" + keyEnum.name();
    }

}
