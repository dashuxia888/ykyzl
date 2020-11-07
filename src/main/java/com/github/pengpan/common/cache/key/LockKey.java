package com.github.pengpan.common.cache.key;

import com.github.pengpan.common.cache.BaseKeyEnum;
import com.github.pengpan.common.cache.KeyAssembly;
import com.github.pengpan.common.cache.ModuleKeyEnum;

public class LockKey extends KeyAssembly {

    public enum KeyEnum implements BaseKeyEnum {
        USER,
        ;

        @Override
        public ModuleKeyEnum module() {
            return ModuleKeyEnum.LOCK;
        }
    }

    public static String lockUser(long userId) {
        return lockKey(KeyEnum.USER, userId);
    }

    public static String lockKey(KeyEnum keyEnum) {
        return assemble(keyEnum);
    }

    public static <T> String lockKey(KeyEnum keyEnum, T id) {
        return assemble(keyEnum, id);
    }
}
