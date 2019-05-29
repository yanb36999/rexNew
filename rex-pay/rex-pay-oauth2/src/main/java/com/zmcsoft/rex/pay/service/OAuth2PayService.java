package com.zmcsoft.rex.pay.service;

import com.zmcsoft.rex.pay.PayRequest;
import com.zmcsoft.rex.pay.PayResponse;
import com.zmcsoft.rex.pay.RexPayService;
import org.hswebframework.web.authorization.oauth2.client.OAuth2RequestService;
import org.hswebframework.web.authorization.oauth2.client.request.OAuth2Request;
import org.hswebframework.web.service.oauth2.OAuth2CrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class OAuth2PayService implements RexPayService {

    @Autowired
    private OAuth2RequestService requestService;

    @Value("${com.zmcsoft.pay.oauth2.serviceId:pay-server}")
    private String serviceId = "pay-server";

    @Override
    public PayResponse requestPay(String chanel, PayRequest request) {
        OAuth2Request oAuth2Request = requestService.create(serviceId)
                .byClientCredentials()
                .request("pay/" + chanel);
        request.getParameters().forEach(oAuth2Request::param);
        return oAuth2Request.post().as(PayResponse.class);
    }

    @Override
    public String callback(String chanel, PayRequest request) {
        OAuth2Request oAuth2Request = requestService.create(serviceId)
                .byClientCredentials()
                .request("pay/" + chanel + "/callback");
        request.getParameters().forEach(oAuth2Request::param);
        return oAuth2Request.post()
                .as(String.class);
    }

    @Override
    public boolean markRepeatPay(String payDetailId) {
        OAuth2Request oAuth2Request = requestService.create(serviceId)
                .byClientCredentials()
                .request("pay/"+payDetailId+"/repeat");

        return oAuth2Request.post().status()==200;
    }
}
