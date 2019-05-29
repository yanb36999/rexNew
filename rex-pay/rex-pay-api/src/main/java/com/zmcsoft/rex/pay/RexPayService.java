package com.zmcsoft.rex.pay;

public interface RexPayService {

    String pay_status_new="0";//待支付

    String pay_status_ok="1";//支付成功

    String pay_status_fail="2";//支付失败

    String pay_status_repeat="11";//重复支付

    String pay_status_invalidate="-1";//无效支付

    PayResponse requestPay(String chanel,PayRequest request);

    String callback(String chanel,PayRequest request);

    boolean markRepeatPay(String payDetailId);
}
