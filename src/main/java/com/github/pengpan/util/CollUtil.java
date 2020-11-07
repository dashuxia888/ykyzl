package com.github.pengpan.util;

import java.util.Collection;

public class CollUtil {

    public static <T> boolean isEmpty(Collection<T> coll) {
        return coll == null || coll.isEmpty();
    }

    public static <T> boolean isNotEmpty(Collection<T> coll) {
        return !isEmpty(coll);
    }
}
