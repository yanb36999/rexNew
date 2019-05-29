package com.zmcsoft.rex.message;

import org.hswebframework.utils.ClassUtils;

/**
 * Created by zhouhao on 2017/11/7.
 */
public abstract class AbstractMessageSenderProvider<T extends MessageSender> implements MessageSenderProvider<T> {

    private String provider;
    private Class<T> type;

    @SuppressWarnings("unchcked")
    public AbstractMessageSenderProvider() {
        type = (Class) ClassUtils.getGenericType(this.getClass());
    }

    @Override
    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    @Override
    public Class<T> getProviderClass() {
        return type;
    }
}
