package com.zmcsoft.rex.commons.district.api.utils;


/**
 * 已启用 请使用 {@link com.zmcsoft.rex.utils.BeanUtils}替代
 * @author zhouhao
 * @since 1.0
 * @see  com.zmcsoft.rex.utils.BeanUtils
 */
@Deprecated
public class BeanUtils {
    public static <T> T merge(Object source, T target) {
        return com.zmcsoft.rex.utils.BeanUtils.merge(source,target);
    }

}
