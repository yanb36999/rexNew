package com.zmcsoft.rex.message.sms;

import com.zmcsoft.rex.message.MessageSender;

public interface SMSSender extends MessageSender {

    SMSSender to(String phone);

    SMSSender content(String content);

}
