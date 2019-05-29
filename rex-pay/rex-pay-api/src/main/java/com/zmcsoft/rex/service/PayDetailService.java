package com.zmcsoft.rex.service;

import com.zmcsoft.rex.entity.PayDetail;
import org.hswebframework.web.service.CrudService;

import java.util.List;
import java.util.Map;


/**
 * 支付订单详情
 * @author zhouhao
 * @since 1.0
 */
public interface PayDetailService extends CrudService<PayDetail, String> {

    PayDetail selectByPaySerialId(String serId);

    PayDetail selectByPaySerialIdAndStatusAndChannelId(String serId, String status, String channelId);

    List<PayDetail> selectByBookDate(String bookDate);

    Map<String, String> selectICBCRealPayInfo(PayDetail detail);

}
