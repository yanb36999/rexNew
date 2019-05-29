package com.zmcsoft.rex.message;

import com.zmcsoft.rex.message.builder.MessageSenderFactory;

public interface MessageSenderProvider<T extends MessageSender> extends MessageSenderFactory<T > {
    String getProvider();

    Class<T> getProviderClass();
}
