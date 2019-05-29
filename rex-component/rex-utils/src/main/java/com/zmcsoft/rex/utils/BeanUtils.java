package com.zmcsoft.rex.utils;

import com.alibaba.fastjson.JSON;
import org.apache.commons.collections.BeanMap;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author zhouhao
 * @since
 */
public class BeanUtils {
    public static <T> T merge(Object source, T target) {
        try {
            Map<String, Object> mapObject;
            if (source instanceof String) {
                mapObject = JSON.parseObject((String) source);
            } else {
                mapObject = new HashMap<>(new BeanMap(source));
            }
            mapObject.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue() == null || "".equals(entry.getValue()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet())
                    .forEach(mapObject::remove);

            mapObject.forEach((key, value) -> {
                try {
                    org.apache.commons.beanutils.BeanUtils.setProperty(target, key, value);
                } catch (Exception ignore) {
                }
            });
        } catch (Exception ignore) {
            throw new UnsupportedOperationException(ignore);
        }
        return target;
    }

}
