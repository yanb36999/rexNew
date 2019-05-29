package com.zmcsoft.rex.commons.district.api.utils;


import java.util.*;
import java.util.function.Function;

/**
 * @see com.zmcsoft.rex.utils.NullUtils
 */
@Deprecated
public class NullUtils {

    public static <K, V> Object transNullToEmpty(Object data, Function<Object, Map<K, V>> mapConvert,Function<String,Object>newValue) {
       return com.zmcsoft.rex.utils.NullUtils.transNullToEmpty(data,mapConvert,newValue);
    }
}
