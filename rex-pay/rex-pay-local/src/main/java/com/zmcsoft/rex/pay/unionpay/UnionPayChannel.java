package com.zmcsoft.rex.pay.unionpay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.zmcsoft.rex.entity.PayDetail;
import com.zmcsoft.rex.pay.*;
import com.zmcsoft.rex.service.PayDetailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j(topic = "business.pay.unionpay")
@Service
public class UnionPayChannel extends AsyncCallbackSupport implements RexPayChannel, RexPayChannelSupplier {
    private String key = "2a0C1L7z1S1j0S9P";

    private String account = "4d6a718c68504ad9b12cc85626b2c0fb";

    private String apiUrl = "http://interface.uphsh.com/api/";

    @Value("${com.zmcsoft.pay.unionpay.callback}")
    private String callback = "http://cdjjjf.rex.cdjg.gov.cn:8090/pay/unionpay/callback";

    @Autowired
    private PayDetailService payDetailService;

    public static void main(String[] args) {

        System.out.println(new BigDecimal("918").divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));

//        UnionPayChannel unionPayChannel = new UnionPayChannel();
//
//        PayResponse response = unionPayChannel.requestPay(new PayRequest());
//
//        System.out.println(response);
    }

    @Override
    public PayResponse requestPay(PayRequest payRequest) {
        try {

            String redirectUrl = payRequest.getParameter("redirectUrl", null);
            String callbackUrl = payRequest.getParameter("callback", null);

            //5110251406872423 5101041446990633
            String payNumber = payRequest.getParameter("orderId", null);

            Objects.requireNonNull(payNumber, "orderId can not be null");

            Map<String, Object> data = new TreeMap<>();

            String type = "66";

            data.put("paynumber", Base64.encodeBase64String(DESUtils.encrypt(payNumber.getBytes(), key)));
            data.put("backurl", Base64.encodeBase64String(DESUtils.encrypt(callback.getBytes(), key)));
            data.put("frontBackUrl", Base64.encodeBase64String(DESUtils.encrypt(redirectUrl.getBytes(), key)));

            String jsonData = JSON.toJSONString(data);
            String signStr = type + "&" + account + "&" + jsonData + "&&" + key;
            String sign = DigestUtils.md5Hex(signStr).toUpperCase();

            log.info("发起银联支付请求:{},callback={},data={},sign={}", payRequest.getParameters(), callback, jsonData, sign);

            String result = requestBuilder.http(apiUrl + "traffic/charge")
                    .param("type", type)
                    .param("account", account)
                    .param("sign", sign)
                    .param("data", jsonData)
                    .post()
                    .asString();


            JSONObject object = JSON.parseObject(result);
            String status = object.getString("statu");

            log.info("发起银联支付请求结果:{}", result);

            if ("S01".equals(status)) {
                log.info("发起银联支付请求成功:{}", object);
                CallbackCache callbackCache = CallbackCache.builder()
                        .redirectUrl(redirectUrl)
                        .callbackUrl(callbackUrl)
                        .request(payRequest).build();
                String html = object.getJSONObject("data").getString("html");

                //插入流水表
                PayDetail payDetail = PayDetail.builder()
                        .paySerialId(payNumber)
                        .channelId(getChannel())
                        .payReturnUrl(redirectUrl)
                        .callbackUrl(callbackUrl)
                        .callbackStatus("0")
                        .payStatus(RexPayService.pay_status_new) //已提交
                        .callbackData(JSON.toJSONString(callbackCache))////回调数据,在支付成功后,对业务系统进行回调时使用到此数据
                        .createTime(new Date())
                        .build();
                payDetailService.insert(payDetail);
                return PayResponse.builder().htmlForm(html).success(true).message("请求成功").build();
            } else {
                return PayResponse.builder().message(object.getString("message")).success(false).build();
            }
        } catch (IOException e) {
            log.error("发起银联支付失败", e);
        }

        return PayResponse.builder().build();
    }


    @Override
    public String callback(PayRequest payRequest) {
        log.info("收到银联支付回调:\n{}", JSON.toJSONString(payRequest.getParameters(), SerializerFeature.PrettyFormat));
        //sign=9ADBBC82B71C13988A3CFB1D6BF75323, amount=5000, pay
        //number=15228847233, state=0, orderno=MSN10010735170728101843347, type=02,  billid=PYD20170728101844544857

        String payNumber = payRequest.getParameter("paynumber");
        String state = payRequest.getParameter("state");
        String sign = payRequest.getParameter("sign");
        String amount = payRequest.getParameter("amount");
        String billid = payRequest.getParameter("billid");
        String type = payRequest.getParameter("type");
        //该笔交易缴费成功
        boolean success = "1".equals(state);

        PayDetail detail = payDetailService.selectByPaySerialIdAndStatusAndChannelId(payNumber, "0", "unionpay");
        if (null == detail) {
            log.error("银联支付回调的订单不存在:{}", payNumber);
        } else {
            CallbackCache callbackCache = JSON.parseObject(detail.getCallbackData(), CallbackCache.class);

            //金额以分为单位,除以100四舍五入保留2位
            BigDecimal decimalAmount = new BigDecimal(amount)
                    .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);

            PayDetail newDetail = PayDetail.builder()
                    .payStatus("1".equals(state) ? RexPayService.pay_status_ok : RexPayService.pay_status_fail)
                    .payReturnTime(new Date())
                    .payStatusRemark(type)
                    .channelSerialId(billid)
                    .amount(decimalAmount)
                    .build();

            payDetailService.updateByPk(detail.getId(), newDetail);

            //回调给业务系统的特定参数
            Map<String, String> callbackParam = new HashMap<>();
            callbackParam.put("success", String.valueOf(success));
            callbackParam.put("comment", String.valueOf(type));
            callbackParam.put("id", detail.getId());

            //放到请求的参数中,如果失败,将一起更新到数据库中
            callbackCache.getRequest().getParameters().putAll(callbackParam);

            String callbackDataJson = JSON.toJSONString(callbackCache, SerializerFeature.PrettyFormat);
            //尝试调用回调
            log.info("尝试调用回调:{}", callbackDataJson);
            addHttpCallback(callbackCache, httpRequest -> {
                //传入是否成功等参数
                httpRequest.params(callbackParam);
            }, () -> {
                //回调成功
                PayDetail callbacklInfo = PayDetail.builder()
                        .callbackStatus("1")
                        .callbackData(callbackDataJson)
                        .build();
                payDetailService.updateByPk(detail.getId(), callbacklInfo);
            }, () -> {
                //回调失败
                PayDetail callbacklInfo = PayDetail.builder()
                        .callbackStatus("-1")
                        .remark("调用业务系统回调失败")
                        .callbackData(callbackDataJson)
                        .build();
                payDetailService.updateByPk(detail.getId(), callbacklInfo);
            });
        }
        return "OK";
    }

    @Override
    public String getChannel() {
        return RexPayChannel.unionpay;
    }

    @Override
    public RexPayChannel get() {
        return this;
    }
}
