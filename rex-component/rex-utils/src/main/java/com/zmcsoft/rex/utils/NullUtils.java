package com.zmcsoft.rex.utils;


import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NullUtils {

    private static Class basicType[] = {
            Number.class, String.class, Date.class
    };

    private static <K, V> Map tryTransObjectToMap(Object o, Function<Object, Map<K, V>> mapConvert) {
        if (o instanceof Map) {
            return (Map) o;
        }
        if (Arrays.stream(basicType).anyMatch(t -> t.isInstance(o))) {
            return null;
        }
        return mapConvert.apply(o);
    }

    public static <K, V> Object transNullToEmpty(Object data, Function<Object, Map<K, V>> mapConvert,Function<String,Object>newValue) {
        Map<String,Object> mapValue = tryTransObjectToMap(data,mapConvert);
        if (mapValue==null){
            return data;
        }
        mapValue.entrySet().forEach(entry -> {
            Object value = entry.getValue();
            if(value ==null){
                entry.setValue(newValue.apply(entry.getKey()));
            }else if(value instanceof List){
                value= ((Collection) value).stream().map(val->transNullToEmpty(val,mapConvert,newValue)).collect(Collectors.toList());
                entry.setValue(value);
            } else{
                entry.setValue(transNullToEmpty(value,mapConvert,newValue));
            }
        });
        return mapValue;
    }
}
