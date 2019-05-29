package com.zmcsoft.rex.pay.service;

import com.zmcsoft.rex.entity.PayDetail;
import com.zmcsoft.rex.service.PayDetailService;
import org.hswebframework.web.service.oauth2.AbstractOAuth2CrudService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class OAuth2PayDetailService extends AbstractOAuth2CrudService<PayDetail, String> implements PayDetailService {
    @Override
    public PayDetail selectByPaySerialId(String serId) {
        return createRequest("/serial/" + serId).get().as(PayDetail.class);
    }

    @Override
    public PayDetail selectByPaySerialIdAndStatusAndChannelId(String serId, String status, String channelId) {
        return createRequest("/serial/" + serId + "/" + status + "/" + channelId).get().as(PayDetail.class);
    }

    @Override
    public List<PayDetail> selectByBookDate(String bookDate) {
        return createRequest("/book-date/" + bookDate).get().asList(PayDetail.class);
    }

    @Override
    @SuppressWarnings("all")
    public Map<String, String> selectICBCRealPayInfo(PayDetail detail) {
        return (Map) createRequest("/icbc-pay-info/",detail).get().as(Map.class);
    }

    @Override
    public String getServiceId() {
        return "pay-server";
    }

    @Override
    public String getUriPrefix() {
        return "/pay-detail";
    }

    @Override
    public PayDetail createEntity() {
        return PayDetail.builder().build();
    }

    @Override
    public Class<PayDetail> getEntityInstanceType() {
        return PayDetail.class;
    }
}
