package com.zmcsoft.rex.message;

import com.zmcsoft.rex.message.builder.MessageSenderFactory;
import com.zmcsoft.rex.message.ftp.FTPMessageSender;
import com.zmcsoft.rex.message.sms.SMSSender;
import com.zmcsoft.rex.message.wechat.WechatMessageSender;

import java.util.HashMap;
import java.util.Map;

public class DefaultMessageSenders implements MessageSenders {

    private static final MessageSenderFactory NULL = () -> null;

    private Map<Class, Map<String, MessageSenderFactory>> repo = new HashMap<>();

    @SuppressWarnings("unchecked")
    private <T> T getSender(Class clazz,String provider){
        return (T)(repo.computeIfAbsent(clazz,c->new HashMap<>())
                .computeIfAbsent(provider,p->NULL).create());
    }

    @Override
    public WechatMessageSender wechat(String provider) {
        return getSender(WechatMessageSender.class,provider);
    }

    @Override
    public FTPMessageSender ftp(String provider) {
        return getSender(FTPMessageSender.class,provider);
    }

    @Override
    public SMSSender sms(String provider) {
        return getSender(SMSSender.class,provider);
    }

    public void register(MessageSenderProvider provider){
        repo.computeIfAbsent(provider.getProviderClass(),c->new HashMap<>())
                .put(provider.getProvider(),provider);
    }
}
