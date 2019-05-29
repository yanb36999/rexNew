package com.zmcsoft.rex.pay;

import com.zmcsoft.rex.entity.PayDetail;
import com.zmcsoft.rex.service.PayDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

@Slf4j(topic = "business.pay")
public class DefaultRexPayService implements RexPayService {

    private Map<String, RexPayChannel> supportChannel = new HashMap<>();

    @Autowired
    private PayDetailService payDetailService;

    @Override
    public PayResponse requestPay(String chanel, PayRequest request) {
        RexPayChannel channel = supportChannel.get(chanel);
        if (channel == null) {
            throw new UnsupportedOperationException("pay channel " + chanel + " not support. all channel:" + supportChannel.keySet());
        }
        return channel.requestPay(request);
    }

    @Override
    public String callback(String chanel, PayRequest request) {
        RexPayChannel channel = supportChannel.get(chanel);
        if (channel == null) {
            throw new UnsupportedOperationException("pay channel " + chanel + " not support. all channel:" + supportChannel.keySet());
        }
        return channel.callback(request);
    }

    public void registerChannel(String channel, RexPayChannel payChannel) {
        supportChannel.put(channel, payChannel);
    }

    @Override
    public boolean markRepeatPay(String payDetailId) {
        PayDetail old = payDetailService.selectByPk(payDetailId);
        if (old == null) {
            return false;
        }
        if (RexPayService.pay_status_ok.equals(old.getPayStatus())) {
            log.info("标记:{}为重复缴费!", payDetailId);
            PayDetail detail = PayDetail.builder()
                    .payStatus(pay_status_repeat)
                    .remark((old.getRemark() == null ? "" : old.getRemark()) + "\n重复缴费!")
                    .build();
            payDetailService.updateByPk(payDetailId, detail);
            return true;
        } else {
            log.warn("标记:{}为重复缴费失败,此订单不为支付成功!", payDetailId);
        }
        return false;
    }
}
