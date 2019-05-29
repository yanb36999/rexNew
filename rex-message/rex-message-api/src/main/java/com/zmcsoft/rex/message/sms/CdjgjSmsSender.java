package com.zmcsoft.rex.message.sms;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zmcsoft.rex.message.AbstractAsyncMessageSender;
import jdk.nashorn.internal.parser.JSONParser;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.expands.request.RequestBuilder;
import org.hswebframework.expands.request.SimpleRequestBuilder;
import org.hswebframework.expands.request.webservice.WebServiceRequestInvoker;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhouhao
 * @since 1.0
 */
@Slf4j(topic = "business.sms")
public class CdjgjSmsSender extends AbstractAsyncMessageSender implements SMSSender {

    private static final List<String> CHINA_NET     = Arrays.asList(
            "133", "153", "173", "177",
            "180", "181", "189", "199");
    private static final List<String> CHINA_MOBILE  = Arrays.asList(
            "134", "135", "136", "137",
            "138", "139", "150", "151",
            "152", "157", "158", "159",
            "178", "182", "183", "184",
            "187", "188", "198", "148");
    private static final List<String> CHINA_UNI_COM = Arrays.asList(
            "130", "131", "132", "155",
            "156", "175", "176", "185",
            "186", "166");

    private static final RequestBuilder requestBuilder = new SimpleRequestBuilder();

    private static final String API_URL = "http://118.112.186.45:7788/webService/RegisterSms?wsdl";

    private static final String USERNAME = "dzjc_jcj";
    @SuppressWarnings("all")
    private static final String PASSWORD = "jcj";

    private static WebServiceRequestInvoker invoker;

    static {
        try {
            invoker = requestBuilder.webService()
                    .wsdl(API_URL)
                    .request();

            log.info("初始化短信发送api object:{}", invoker!=null);
        } catch (Exception e) {
            log.error("初始化短信发送api失败", e);
        }
    }

    public String getChanel(String phone) {
        String start = phone.substring(0, 3);
        if (CHINA_NET.contains(start)) {
            return "电信";
        }
        if (CHINA_UNI_COM.contains(start)) {
            return "联通";
        }
        if (CHINA_MOBILE.contains(start)) {
            return "移动";
        }
        log.warn("无法获取号码的运营商:{}", phone);
        return "移动";
    }




    public static void main(String[] args) throws Exception {


        /*
        File file = new File("/Users/luo-mac/Documents/工作/wf_sms.txt");
        BufferedReader reader = null;
        try {

            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            int line = 1;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {

                HashMap o = JSONObject.parseObject(tempString, HashMap.class);
                String phone = (String) o.get("phone");
                String plateNo = (String) o.get("plateNo");
                String illegaltime = (String) o.get("illegaltime");
                String roadName = (String) o.get("roadName");
                String illegalBehaviorName = (String) o.get("illegalBehaviorName");
                String enterOfficeName = (String) o.get("enterOfficeName");
                String lsh = (String) o.get("lsh");

                log.info("短信发送流水号:{} 行号:{}", lsh, line);
                System.out.println("短信发送流水号:{}" + lsh);
                line ++ ;

                CdjgjSmsSender smsSender = new CdjgjSmsSender();
                smsSender.to(phone)
                    .content("成都交警提示：根据“蓉e行”交通众治联盟市民提供的交通违法举报线索，"+"川"+plateNo+"于"+illegaltime+"在"+roadName+"实施"+illegalBehaviorName+"，" +
                        "经调查复核，交通违法行为事实清楚，请于15日内到"+enterOfficeName+"" +
                        "处理或者关注成都交警微信公众号“蓉e行”平台在线接受处理。")
                    .send();


            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        */

//
//        String json="";
//
//        List<HashMap> list = JSONObject.parseArray(json, HashMap.class);
//
//
//        for (HashMap o:list) {
//
//            String phone = (String) o.get("phone");
//            String plateNo = (String) o.get("plateNo");
//            String illegaltime = (String) o.get("illegaltime");
//            String roadName = (String) o.get("roadName");
//            String illegalBehaviorName = (String) o.get("illegalBehaviorName");
//            String enterOfficeName = (String) o.get("enterOfficeName");
//            String lsh = (String) o.get("lsh");
//
//            log.info("短信发送流水号:{}", lsh);
//            System.out.println("短信发送流水号:{}" + lsh);

//            CdjgjSmsSender smsSender = new CdjgjSmsSender();
//            smsSender.to(phone)
//                .content("成都交警提示：根据“蓉e行”交通众治联盟市民提供的交通违法举报线索，"+""+plateNo+"于"+illegaltime+"在"+roadName+"实施"+illegalBehaviorName+"，" +
//                        "经调查复核，交通违法行为事实清楚，请于15日内到"+enterOfficeName+"" +
//                        "处理或者关注成都交警微信公众号“蓉e行”平台在线接受处理。")
//                .send();


//        }





//        CdjgjSmsSender smsSender = new CdjgjSmsSender();
//        smsSender.content("成都交警提示：根据提供的交通违法举报线索，川A1B12S于（2017年12月1日）在（成都市区）实施（闯红灯），" +
//                "经调查复核，交通违法行为事实清楚，请于15日内到（成都交警第二分局）" +
//                "处理或者关注成都交警微信公众号“蓉e行”平台在线接受处理。(test)")
//                .to("18000570393")
//                .send();


    }



    @Override
    protected boolean doSend() {
        String chanel = getChanel(phone);
        try {
            log.info("开始发送短信:{}({})\n{}", phone, chanel, content);
            Integer res = invoker.invoke(
                    USERNAME,
                    PASSWORD,
                    //手机号码
                    phone,
                    //优先级
                    "10",
                    //运营商
                    chanel,
                    //类型固定为J
                    "J",
                    //短信内容
                    content)
                    .get();
            log.error("推送短信,状态:{} phone:{}({}) \n {}", res, phone, chanel, content);
            if (Integer.valueOf(1).equals(res)) {
                return true;
            }
        } catch (Exception e) {
            log.error("推送短信失败,phone:{}({}) \n {}", phone, chanel, content, e);
        }
        return false;
    }

    private String phone;

    private String content;

    @Override
    public SMSSender to(String phone) {
        this.phone = phone;
        return this;
    }

    @Override
    public SMSSender content(String content) {
        this.content = content;
        return this;
    }
}
