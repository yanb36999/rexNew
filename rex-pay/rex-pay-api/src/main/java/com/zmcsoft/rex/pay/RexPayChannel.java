package com.zmcsoft.rex.pay;

public interface RexPayChannel {
    String icbc="icbc";

    String unionpay="unionpay";

    PayResponse requestPay(PayRequest payRequest);

    String callback(PayRequest payRequest);
}
