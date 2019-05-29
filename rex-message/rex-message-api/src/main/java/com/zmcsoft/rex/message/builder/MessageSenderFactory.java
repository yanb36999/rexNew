package com.zmcsoft.rex.message.builder;

import com.zmcsoft.rex.message.MessageSender;

public interface MessageSenderFactory<T extends MessageSender> {
    T create();
}
