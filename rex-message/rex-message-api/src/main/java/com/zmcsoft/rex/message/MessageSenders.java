package com.zmcsoft.rex.message;


import com.zmcsoft.rex.message.ftp.FTPMessageSender;
import com.zmcsoft.rex.message.sms.SMSSender;
import com.zmcsoft.rex.message.wechat.WechatMessageSender;

/**
 * 消息推送工具，用于向各种渠道发送消息
 */
public interface MessageSenders {
    String DEFAULT_PROVIDER = "DEFAULT";

    /**
     * @return 默认的微信消息推送工具
     * @see MessageSenderProvider#getProvider() == DEFAULT
     */
    default WechatMessageSender wechat() {
        return wechat(DEFAULT_PROVIDER);
    }

    /**
     * @return 默认的ftp消息推送工具
     * @see MessageSenderProvider#getProvider() == DEFAULT
     */
    default FTPMessageSender ftp() {
        return ftp(DEFAULT_PROVIDER);
    }

    /**
     * @return 默认的短信推送工具
     * @see MessageSenderProvider#getProvider() == DEFAULT
     */
    default SMSSender sms() {
        return sms(DEFAULT_PROVIDER);
    }

    /**
     * 特定的微信消息推送工具,在提供了多个消息推送渠道的时候，通过其他渠道发送微信推送
     *
     * @param provider provider
     * @return 微信消息推送工具
     */
    WechatMessageSender wechat(String provider);

    /**
     * 获取特定的ftp推送工具
     *
     * @param provider provider
     * @return 特定的ftp推送工具
     */
    FTPMessageSender ftp(String provider);

    /**
     * 获取特定的短信推送工具
     *
     * @param provider provider
     * @return 特定的短信推送工具
     */
    SMSSender sms(String provider);
}
