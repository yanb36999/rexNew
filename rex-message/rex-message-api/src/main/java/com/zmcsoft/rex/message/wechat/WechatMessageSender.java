package com.zmcsoft.rex.message.wechat;

import com.zmcsoft.rex.message.MessageSender;

import java.util.Map;

public interface WechatMessageSender extends MessageSender {
     WechatMessageSender to(String openId);

     WechatMessageSender content(String content);

     WechatMessageSender keyword(String keyword);

     WechatMessageSender title(String title);

     WechatMessageSender template(String key,String value);

     WechatMessageSender template(Map<String,String> templateVar);

}
