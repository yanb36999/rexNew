package com.zmcsoft.rex.pay.icbc;

import cn.com.infosec.icbc.ReturnValue;
import cn.com.infosec.jce.provider.InfosecProvider;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.zmcsoft.rex.entity.PayDetail;
import com.zmcsoft.rex.pay.*;
import com.zmcsoft.rex.service.PayDetailService;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.util.Base64;
import org.dom4j.*;
import org.hswebframework.utils.time.DateFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.*;
import java.util.*;

@Slf4j(topic = "business.pay.icbc")
@Service
@ConfigurationProperties(prefix = "com.zcmsoft.rex.pay.icbc")
public class ICBCPayChannel extends AsyncCallbackSupport implements RexPayChannel, RexPayChannelSupplier {


    private String merId = "4402EE20210014";

    private String merAcct = "4402208011921001241";

    @Value("${com.zmcsoft.pay.icbc.callback}")
    private String callback = "http://cdjjjf.rex.cdjg.gov.cn:8090/pay/icbc/callback";

    private String merCertKeyPassword = "CDJJREX";

    @Autowired
    private PayDetailService payDetailService;

//    private Map<String, String> returnUrlCache = new ConcurrentHashMap<>();

    private static final byte[] crt;
    private static final byte[] key;

    private static final String htmlTemplate = "<form id=\"icbc_pay_request\" action=\"https://mywap2.icbc.com.cn/ICBCWAPBank/servlet/ICBCWAPEBizServlet\" method=\"post\">" +
            "    <input name=\"interfaceName\" type=\"text\" value=\"ICBC_WAPB_B2C\">" +
            "    <input name=\"interfaceVersion\" type=\"text\" value=\"1.0.0.6\">" +
            "    <input name=\"clientType\" type=\"text\" value=\"0\">" +
            "    <input name=\"tranData\" type=\"text\" value=\"%s\">" +
            "    <input name=\"merSignMsg\" type=\"text\" value=\"%s\">" +
            "    <input name=\"merCert\" type=\"text\"  value=\"%s\">" +
            "</form>" +
            "<script type=\"text/javascript\">" +
            "    document.getElementById(\"icbc_pay_request\").submit();" +
            "</script>";

    static {
        Security.addProvider(new InfosecProvider());

        String keyBase64 = "BKeijbQI2rv3IwjC20f+3BllNR51WiDmE7MrMmNPC9Z/8Z1IKiXeukphk/NCs8jQjbiizd/WGghs9Yz1gG1RZyL00kXKMssggTUOzwlYLXyjtUgvXVtg0B/ucHmTfL5yR7QCi7bW5RQnLwb+66/k86mPzCFYj/CUadYfiEQ7zSinYJz3kTNBqQ29ASx3Mxl9GlC3PDCTq+8F7Pme4xnuqppQpSeQLtx50Y57W4sN36EDOZJz2Q8TydQQKtYZ14i/DaG4SyEB1IQrh8sixeIYL17cVPxm66T5xQjN6MBe9ggxt43QktcUUVe948TPkiQAcWJKW6Y9lYjr6s9U1oIHWLS6zzOVO1TcDMIDHLAnAQjJ03CPU60DhdcHInOuXa/BENGBZY4V0rji6rg08AvWDqf/5J4CV7u9HXLaSoj3NrIZ1ismr5A9e2hl3t8EIs7OW6slInd5unRtWPaDVrSeQFXg0MCQ333sL00yHJtls4sYVeCMxpPe06iK0iV7XUAAj7e9/D2vfQyR4HdEgVdgE6GcRe5okVBEEmKaex6HLyMdJOF61jOKVX8dCei19HMAuE/JUordKQDMkj3zMdg+Uh9hlFY34e3yDJx6zJ19WQSz8CtP/BbinpVdmkicM3J9+vX3eNSq1j0VfGdHezzNOA2twtUq2RSwEA0UAtYu7n+MvH0PX7D2YQboBO8ASy2Zs/wVpifC+9bgiir/tkExpwYkCV2gBJjWxrvpQeQvX4ftzdtRZNbTBza1LvtTMcgZ+X8tQr8UQM7Tgvwam+NRCLl09qigpN2UiP1LPK+fqx6Q517NIMlAFhUpEBP6L6EWMrBEotTZBwTuAKwTFbpqJgJLzuiVz6U3kyDaMVtvpOdc/jtrLQyGd7izKX/JZHHZTlLL4vrmajC7U8r0Xh/thCn7Crkwy5y1tUAdCOq7Ee+YtEn1b38Ar5zjc+cN6n5/T8TXAlQ+q+stSKDRShLNdo+SSrTCJH+qXGnKzbuVN+xv9KWy27/Qe+WQ1Dkzy2eo7RtQu+i1zt0vOZLuF5at0McrC0MbSSowybPvYMZXYEwzfNIdjcq++no4uFyAo1oBGY+3rG8cS/yROlj0XilQjLsxNf+qkRTWnEPk4JJZSiluqXI2kAsxlnMmzJ8cDmA9MJrX+adiQ33JzTU8SiN4Y7vI76qGufPQaWj3kbe6WOVS+YVtBNtcyuXftbVnqA1Po1DRmfIucHKKRpRF3JQYpVI2JY9vAuaTSs2wRTBdXLRedsWSsxmzJY2/7TrTE0lq3ebQfUHxlcrr/upcsSipQFYAeQeu5t9lr0XmaCaw6EX7to8nPTgprAKeSeuUhNg33zOYDl5kiHSL6vfMLMQ35NNliuRpxeEafCUDpuivleICOIqPP048rTSq1FmJUxjaL6a6RGHGJW8WJoPyOqOoJ33vXvXG5jcVYAPBSDjmumRN78HFRl4GaZTYncDYC26i4zs98UENinVE5RoiFmbFqmqF3TNveAORJA1KXqXBiZVsd0Dv5AvUH8W0lTh6L1KjDtFa8/LlTXzEgT81pfUhnCCY9eXbLPtCYERi6wXIz32wYLboYohsoSY=";//
        String crtBase64 = "MIIDhzCCAm+gAwIBAgIKYULKEHrkAM+qfzANBgkqhkiG9w0BAQsFADA2MR4wHAYDVQQDExVJQ0JDIENvcnBvcmF0ZSBTdWIgQ0ExFDASBgNVBAoTC2ljYmMuY29tLmNuMB4XDTE3MTEwOTAzMDcwNFoXDTE4MTEwOTAzMDcwNFowPjEXMBUGA1UEAwwOQ0RKSlJFWC5lLjQ0MDIxDTALBgNVBAsMBDQ0MDIxFDASBgNVBAoMC2ljYmMuY29tLmNuMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApeBXmEeTcRg6d98i7Riwb/l/X4osoKWJdSlNEOVejbhtpXgNX4W9gf/akqOAnhlXnKFTOZhnDNDYpMAFdgx8VFHKHvKGu/6yFf2Lqr+PpxFbo21sv5vTU6CssmituZ7RtnfMWRtc7MGkXW1qWAAKzTgD5sFwOIa+XhMcT213ypNUuwNzwpRQPSbvnVl+zBOypf45v5Cri4VE8mZdvz7liFpNb+1Y28+1dlTrlCXfxYlbxuvRW6RBE4/OkPX01+SbQxlq2RnXgVcb6VGwmBZNeCrL8gN/r6JTbNRewJBP2J/C3NoFUOJZnat+Fbi7B1oVusJL4H9DKOlF9fj44i3TnwIDAQABo4GOMIGLMB8GA1UdIwQYMBaAFPnIRcNTkyY2MZMvlBDfyFM99m+eMEkGA1UdHwRCMEAwPqA8oDqkODA2MRAwDgYDVQQDDAdjcmwyNzIyMQwwCgYDVQQLDANjcmwxFDASBgNVBAoMC2ljYmMuY29tLmNuMB0GA1UdDgQWBBSUHWF4Ft9ObhMjOahiy/XfG3F+BjANBgkqhkiG9w0BAQsFAAOCAQEAPXQR+C48Uk6XWOz76Gvw8VyBTZ32DhlCzabNLe06EUT4iM3bYKlGqDg7HT/6J1DTMIF+/3MasEdCGLPtKDJjupW18ZVxz/thytdINqzZQ3FJt7Ith5aYaTP34Rxc2S52co4BrA4oIA94pm2E6kBoMkQK61yMNVl93ie5I9ieQjXC532GaD4liLsxOGR9ZZXs9dQr5ERTGdTc9lTO46j+GYR3WbtU6+JlRefTsH6aZkM5tmwkuXZ3OY+Z/NFpitPBK5ViWw4JGow8QpYTIY/IqhDcXO48/0MFYBfkjJY7uLx2IHD4xTij9gtzJtw+GYSlBxdfReAHEK5SvXKrAqvbnw==";

        key = ReturnValue.base64dec(keyBase64.getBytes());
        crt = ReturnValue.base64dec(crtBase64.getBytes());

        //PathMatchingResourcePatternResolver resolver=new PathMatchingResourcePatternResolver();

//        try (InputStream cerStream =
//                     new FileInputStream("/Users/zhouhao/IdeaProjects/rex-platform/rex-pay/rex-pay-local/src/main/resources/key/icbc/1.crt");
//                    //new ClassPathResource("/key/icbc/1.crt").getInputStream();
////             resolver.getResource("classpath:key/icbc/1.crt").getInputStream();
//             InputStream keyStream =
//                     new FileInputStream("/Users/zhouhao/IdeaProjects/rex-platform/rex-pay/rex-pay-local/src/main/resources/key/icbc/1.key");
////             new ClassPathResource("/key/icbc/1.key").getInputStream()
////             resolver.getResource("classpath:key/icbc/1.key").getInputStream()
//
//        ) {
//            FileInputStream in=    new FileInputStream("/Users/zhouhao/IdeaProjects/rex-platform/rex-pay/rex-pay-local/src/main/resources/key/icbc/1.crt");
//          byte[] inB=  StreamUtils.copyToByteArray(in);
//
////            key = StreamUtils.copyToByteArray(keyStream);
////            crt = StreamUtils.copyToByteArray(cerStream);
//            key=ReturnValue.base64dec(keyBase64.getBytes());
//            crt=ReturnValue.base64dec(crtBase64.getBytes());
//
//            System.out.println(new String(ReturnValue.base64enc(key)));
//            System.out.println(new String(ReturnValue.base64enc(crt)));
//
//            System.out.println(Arrays.equals(inB,crt));
//
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
    }


    @Override
    public String getChannel() {
        return RexPayChannel.icbc;
    }

    @Override
    public RexPayChannel get() {
        return this;
    }

    @Override
    public String callback(PayRequest payRequest) {
        String jsonRequest = JSON.toJSONString(payRequest.getParameters(), SerializerFeature.PrettyFormat);

        log.info("收到工商银行支付回调:\n{}", jsonRequest);

        String orderId = payRequest.getParameter("merVAR", "");

        String notifyData = payRequest.getParameter("notifyData");

        String signMsg = payRequest.getParameter("signMsg");

        CallbackCache callbackCache = null;//cache.get(orderId);

        try {
            byte[] notifyDataBytes = notifyData.getBytes();
            byte[] priKey = ReturnValue.getPrivateKey(key, merCertKeyPassword.toCharArray());
            byte[] cipherText = ReturnValue.privateEncrypt(notifyDataBytes, priKey);
            byte[] pubKey = ReturnValue.getPublicKey(crt);
            byte[] restoredPlaintext = ReturnValue.publicDecrypt(cipherText, pubKey);

            String plainText = new String(Base64.decodeBase64(restoredPlaintext), "gbk");

            // notifyDataBytes=plainText.getBytes();

            //  int verify = ReturnValue.verifySign(notifyDataBytes, notifyDataBytes.length, crt, Base64.decodeBase64(signMsg));

            // System.out.println(verify);

            log.info("解码工商银行支付回调报文成功:\nmerVAR={}\nnotifyData={}", orderId, plainText);

            //更新流水信息
            try {
                Document document = DocumentHelper.parseText(plainText);
                Element root = document.getRootElement();
                List<Node> orderInfo = root.selectNodes("//orderInfo/*");

                List<Node> bankInfo = root.selectNodes("//bank/*");

                Map<String, String> orderInfoMap = new HashMap<>();
                Map<String, String> bankMap = new HashMap<>();

                for (Node node : bankInfo) {
                    bankMap.put(node.getName(), node.getText());
                }
                for (Node node : orderInfo) {
                    orderInfoMap.put(node.getName(), node.getText());
                }
                log.info("解析工商银行报文成功: \norder={} \nbank={}", orderInfoMap, bankMap);
                PayDetail tmp = payDetailService.selectByPaySerialIdAndStatusAndChannelId(orderId, "0", "icbc");
                String tranStat = bankMap.get("tranStat");
                boolean success = "1".equals(tranStat);

                //没有待支付的订单。可能是重复或者超时了的回调.
                //获取上一次处理的订单。如果本次是成功，上次是失败。则重新执行回调
                if (null == tmp) {
                    //获取上次回调的数据
                    tmp = payDetailService.selectByPaySerialIdAndStatusAndChannelId(orderId, null, "icbc");
                    if (null != tmp) {
                        //如果上次是支付成功的,不做处理
                        if (RexPayService.pay_status_ok.equalsIgnoreCase(tmp.getPayStatus())) {
                            log.info("工行重复回调已支付成功的订单,跳过处理:{} \n{}", orderId, bankMap);
                            tmp = null;
                        }
                        //如果本次是支付失败的,不做处理
                        else if (!success) {
                            log.warn("工行重复回调,跳过处理:{} \n{}", orderId, bankMap);
                            tmp = null;
                        }
                    }
                }
                PayDetail detail = tmp;
                if (null == detail) {
                    log.error("工商银行回调的订单不存在:{}\n{}", orderId, bankMap);
                } else {
                    callbackCache = JSON.parseObject(detail.getCallbackData(), CallbackCache.class);

                    PayDetail newDetail = PayDetail.builder()
                            .payStatus("1".equals(tranStat) ? RexPayService.pay_status_ok : RexPayService.pay_status_fail)
                            .payReturnTime(new Date())
                            .payStatusRemark(bankMap.get("comment"))
                            .channelSerialId(bankMap.get("TranSerialNo"))  //把银行流水查回来 2017/11/12
                            .build();

                    Map<String, String> callbackParam = new HashMap<>();
                    callbackParam.put("success", String.valueOf(success));
                    callbackParam.put("comment", String.valueOf(bankMap.get("comment")));
                    callbackParam.put("id", detail.getId());

                    //放到请求的参数中,将一起更新到数据库中
                    callbackCache.getRequest().getParameters().putAll(callbackParam);

                    payDetailService.updateByPk(detail.getId(), newDetail);
                    CallbackCache finalCache = callbackCache;

                    String callbackDataJson = JSON.toJSONString(finalCache, SerializerFeature.PrettyFormat);

                    //尝试调用回调
                    log.info("尝试调用回调:{}", callbackDataJson);
                    addHttpCallback(callbackCache,
                            httpRequest -> {
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
                                        .callbackData(callbackDataJson)
                                        .remark("调用业务系统回调失败")
                                        .build();
                                payDetailService.updateByPk(detail.getId(), callbacklInfo);
                            });
                }
            } catch (DocumentException e) {
                log.error("解析工行回调报文失败:\n{}", plainText, e);
            }

        } catch (Exception e) {
            log.error("解析工行回调报文失败:\n{}", jsonRequest, e);
        }
        if (null != callbackCache) {
            return callbackCache.getRedirectUrl();
        }
        //处理回调数据
        return "";

    }

    @Override
    public PayResponse requestPay(PayRequest request) {
        log.info("发起支付请求:{}\n callback={}", request.getParameters(), callback);


        String data = buildData(request);
        try {
            SignInfo info = new ICBCPayChannel().sign(data);

            String orderid = request.getParameter("orderId", null);
            String redirectUrl = request.getParameter("redirectUrl");
            String callbackUrl = request.getParameter("callback", null);
            BigDecimal amount = new BigDecimal(request.getParameter("amount", "0"));
            CallbackCache callbackCache = CallbackCache.builder()
                    .redirectUrl(redirectUrl)
                    .callbackUrl(callbackUrl)
                    .request(request).build();

            //插入支付流水
            PayDetail payDetail = PayDetail.builder()
                    .paySerialId(orderid)
                    .payReturnUrl(redirectUrl)
                    .callbackUrl(callbackUrl)
                    .channelId(getChannel())
                    .callbackStatus("0")
                    .callbackData(JSON.toJSONString(callbackCache)) //回调数据,在支付成功后,对业务系统进行回调时使用到此数据
                    .amount(amount)
                    .payStatus(RexPayService.pay_status_new) //已提交
                    .createTime(new Date())
                    .build();
            payDetailService.insert(payDetail);
            return PayResponse.builder().success(true).htmlForm(buildHtml(info)).message("发起支付请求成功").build();

        } catch (Exception e) {
            log.error("工商银行支付请求签名失败", e);
            return PayResponse.builder().success(false).message("签名失败").build();
        }
    }


    private String buildHtml(SignInfo info) {
        return String.format(htmlTemplate, info.tranData, info.signMsg, info.certs);
    }


    private static String formatMoney(String money) {
        BigDecimal decimal = new BigDecimal(money);
        return String.valueOf(decimal.multiply(new BigDecimal(100)).longValue());
    }

    private String buildData(PayRequest request) {

        //决定书编号
        String orderid = request.getParameter("orderId", null);// "5101321000482323";//request.getParameter("orderid");
        String merVAR = orderid; //merVar为业务系统传入的真实订单号

        //订单号+时间戳
        // orderid = orderid + "_" + System.currentTimeMillis();

        //罚款金额
        String amount = request.getParameter("amount", null);// formatMoney("0.01");//request.getParameter("amount");
        Objects.requireNonNull(orderid, "orderId cannot be null");
        Objects.requireNonNull(amount, "amount cannot be null");


        amount = formatMoney(amount);
        //1
        String installmentTimes = "1";//request.getParameter("installmentTimes");
        //0
        String verifyJoinFlag = "0";//request.getParameter("verifyJoinFlag");

        //回调
        String merURL = callback; //"http://www.zmcsoft.com/icbc/callback";//request.getParameter("merURL");

        //通知类型
        String notifyType = "HS";// request.getParameter("notifyType");

        String resultType = "0";//request.getParameter("resultType");

        //固定 人民币
        String curType = "001";//request.getParameter("curType");

        String goodsID = request.getParameter("goodsID");
        String goodsName = request.getParameter("goodsName");
        String goodsNum = request.getParameter("goodsNum");
        String carriageAmt = request.getParameter("carriageAmt");

        String Language = request.getParameter("Language");

        String merHint = request.getParameter("merHint");
        String remark1 = request.getParameter("remark1");
        String remark2 = request.getParameter("remark2");

        String orderDate = DateFormatter.toString(new Date(), "yyyyMMddHHmmss");

        String tranData = "<?xml version=\"1.0\" encoding=\"GBK\" standalone=\"no\"?>" +
                "<B2CReq>" +
                "<interfaceName>ICBC_WAPB_B2C</interfaceName>" +
                "<interfaceVersion>1.0.0.6</interfaceVersion>" +
                "<orderInfo>" +
                "<orderDate>" + orderDate + "</orderDate>" +
                "<orderid>" + orderid + "</orderid>" +
                "<amount>" + amount + "</amount>" +
                "<installmentTimes>" + installmentTimes + "</installmentTimes>" +
                "<curType>" + curType + "</curType>" +
                "<merID>" + merId + "</merID>" +
                "<merAcct>" + merAcct + "</merAcct>" +
                "</orderInfo>" +
                "<custom>" +
                "<verifyJoinFlag>" + verifyJoinFlag + "</verifyJoinFlag>" +
                "<Language>" + Language + "</Language>" +
                "</custom>" +
                "<message>" +
                "<goodsID>" + goodsID + "</goodsID>" +
                "<goodsName>" + goodsName + "</goodsName>" +
                "<goodsNum>" + goodsNum + "</goodsNum>" +
                "<carriageAmt>" + carriageAmt + "</carriageAmt>" +
                "<merHint>" + merHint + "</merHint>" +
                "<remark1>" + remark1 + "</remark1>" +
                "<remark2>" + remark2 + "</remark2>" +
                "<merURL>" + merURL + "</merURL>" +
                "<merVAR>" + merVAR + "</merVAR>" +
                "<notifyType>" + notifyType + "</notifyType>" +
                "<resultType>" + resultType + "</resultType>" +
                "<backup1></backup1>" +
                "<backup2></backup2>" +
                "<backup3></backup3>" +
                "<backup4></backup4>" +
                "</message>" +
                "</B2CReq>";

        return tranData;
    }


    public static void main(String[] args) {
        try {
            System.out.println(formatMoney("0.01"));
            ICBCPayChannel service = new ICBCPayChannel();


            String data = service.callback(new PayRequest(new HashMap<String, String>() {
                {
                    put("notifyData", "PD94bWwgIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IkdCSyIgc3RhbmRhbG9uZT0ibm8iID8+PEIyQ1Jlcz48aW50ZXJmYWNlTmFtZT5JQ0JDX1dBUEJfQjJDPC9pbnRlcmZhY2VOYW1lPjxpbnRlcmZhY2VWZXJzaW9uPjEuMC4wLjY8L2ludGVyZmFjZVZlcnNpb24+PG9yZGVySW5mbz48b3JkZXJEYXRlPjIwMTcxMTEwMjIwOTA3PC9vcmRlckRhdGU+PG9yZGVyaWQ+NTEwMTA0MTQ0Njk5MDYzMzwvb3JkZXJpZD48YW1vdW50PjE8L2Ftb3VudD48aW5zdGFsbG1lbnRUaW1lcz4xPC9pbnN0YWxsbWVudFRpbWVzPjxtZXJBY2N0PjQ0MDIyMDgwMTE5MjEwMDEyNDE8L21lckFjY3Q+PG1lcklEPjQ0MDJFRTIwMjEwMDE0PC9tZXJJRD48Y3VyVHlwZT4wMDE8L2N1clR5cGU+PHZlcmlmeUpvaW5GbGFnPjA8L3ZlcmlmeUpvaW5GbGFnPjxKb2luRmxhZz4wPC9Kb2luRmxhZz48VXNlck51bT48L1VzZXJOdW0+PC9vcmRlckluZm8+PGJhbms+PFRyYW5TZXJpYWxObz5IRVowMDAwMDU1ODE1MzgxMTg8L1RyYW5TZXJpYWxObz48bm90aWZ5RGF0ZT4yMDE3MTExMDIyMDk0NDwvbm90aWZ5RGF0ZT48dHJhblN0YXQ+MTwvdHJhblN0YXQ+PGNvbW1lbnQ+vbvS17PJuaajrNLRx+XL46OhPC9jb21tZW50PjwvYmFuaz48L0IyQ1Jlcz4=");
                    put("signMsg", "uzrzL9rzsnVHMEThgaKsAMCuOnXZqSOUjNHu4QiBUDSW4iCIsBI0hfWUMAbQKcUCW8yqu4/z/jO8Sj0HSLxNeSFph38geRN7enZushuyQW9jHuQopn0TXGgNv9GjNx0IZB1BeWj3y4wYqpEXd1riCWb1q+aXV/2w0lSfbyQVuKE=");
                }
            }));
            Document document = DocumentHelper.parseText(data);
            Element root = document.getRootElement();
            List<Node> orderInfo = root.selectNodes("//orderInfo/*");

            List<Node> bankInfo = root.selectNodes("//bank/*");

            Map<String, String> orderInfoMap = new HashMap<>();
            Map<String, String> bankMap = new HashMap<>();

            for (Node node : bankInfo) {
                bankMap.put(node.getName(), node.getText());
            }
            for (Node node : orderInfo) {
                orderInfoMap.put(node.getName(), node.getText());
            }
            System.out.println(bankMap);
            System.out.println(orderInfoMap);
            //      Map<String,String> orderInfoMap = new HashMap<>();

            // System.out.println(data);

            //   SignInfo info = new ICBCPayChannel().sign(data);

            //  System.out.println(service.buildHtml(info));
//            System.out.println(info.certs);
//            System.out.println(info.signMsg);
//            System.out.println(info.tranData);

            // System.out.println(JSON.toJSONString(info, SerializerFeature.PrettyFormat));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private SignInfo sign(String tranData) throws NoSuchAlgorithmException, NoSuchProviderException, SignatureException, InvalidKeyException {
        try {
            byte[] signature = ReturnValue.base64enc(ReturnValue.sign(tranData.getBytes(), tranData.getBytes().length,
                    key, merCertKeyPassword.toCharArray()));

            String signMsg = new String(signature);
            byte[] base64 = ReturnValue.base64enc(tranData.getBytes());
            tranData = new String(base64);

            byte[] cert = ReturnValue.base64enc(crt);
            String certs = new String(cert);

            return SignInfo.builder()
                    .signMsg(signMsg)
                    .tranData(tranData)
                    .certs(certs).build();

        } catch (Exception e) {
            log.error("生成支付报文失败:\ntranData={}", tranData, e);
            throw new RuntimeException(e);
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Setter
    @Getter
    @ToString
    static class SignInfo {
        private String signMsg;
        private String tranData;
        private String certs;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public void setMerAcct(String merAcct) {
        this.merAcct = merAcct;
    }

    public void setMerId(String merId) {
        this.merId = merId;
    }

    public void setMerCertKeyPassword(String merCertKeyPassword) {
        this.merCertKeyPassword = merCertKeyPassword;
    }
}