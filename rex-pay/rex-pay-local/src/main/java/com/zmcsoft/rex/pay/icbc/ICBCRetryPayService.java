package com.zmcsoft.rex.pay.icbc;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.zmcsoft.rex.entity.PayDetail;
import com.zmcsoft.rex.pay.AsyncCallbackSupport;
import com.zmcsoft.rex.pay.RexPayChannel;
import com.zmcsoft.rex.pay.RexPayService;
import com.zmcsoft.rex.pay.impl.dao.PayDetailDao;
import com.zmcsoft.rex.service.PayDetailService;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.BusinessException;
import org.hswebframework.web.concurrent.lock.annotation.Lock;
import org.hswebframework.web.service.DefaultDSLQueryService;
import org.hswebframework.web.service.DefaultDSLUpdateService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.zmcsoft.rex.pay.RexPayService.pay_status_invalidate;
import static com.zmcsoft.rex.pay.RexPayService.pay_status_new;

@Slf4j(topic = "business.pay.icbc.retry")
@Service
@Profile("prod")
public class ICBCRetryPayService extends AsyncCallbackSupport {

    @Autowired
    private PayDetailService payDetailService;

    @Autowired
    private PayDetailDao payDetailDao;

    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(16);
//
//    @Autowired
//    private OAuth2RequestService oAuth2RequestService;

    @Value("${icbc.pay.oauth2.id:icbc-server-test}")
    private String serviceId = "icbc-server-test";


    private void retryCallback(PayDetail payDetail) {
        retryCallback(payDetail, null);
    }

    private void retryCallback(PayDetail payDetail, Map<String, String> param) {
        String callbackData = payDetail.getCallbackData();

        CallbackCache callbackCache = JSON.parseObject(callbackData, CallbackCache.class);
        callbackCache.getRequest().getParameters().put("id", payDetail.getId());

        if (null != param) {
            callbackCache.getRequest().getParameters().putAll(param);
        }
        String callbackDataJson = JSON.toJSONString(callbackCache, SerializerFeature.PrettyFormat);
        addHttpCallback(callbackCache,
                httpRequest -> {
                    //传入是否成功等参数
                    if (null != param) {
                        httpRequest.params(param);
                    }
                }, () -> {
                    //回调成功
                    PayDetail callbacklInfo = PayDetail.builder()
                            .callbackStatus("1")
                            .build();
                    log.info("重试回调成功!callbacklInfo:{}", JSONObject.toJSONString(callbacklInfo));
                    payDetailService.updateByPk(payDetail.getId(), callbacklInfo);
                }, () -> {
                    //回调失败
                    PayDetail callbacklInfo = PayDetail.builder()
                            .callbackStatus("-1")
                            .callbackData(callbackDataJson)
                            .remark("重试调用业务系统回调失败")
                            .build();
                    log.info("重试回调失败!callbacklInfo:{}", JSONObject.toJSONString(callbacklInfo));
                    payDetailService.updateByPk(payDetail.getId(), callbacklInfo);
                });
    }

    @PostConstruct
    public void init() {
        new Thread(() -> {
            try {
                Thread.sleep(60 * 1000);
                retryPayBySuccess();
            } catch (Exception e) {
            }
        }).start();
    }

    /**
     * 自动执行，针对工行已经付款成功，但是由于各种原因，没有更新用户付款状态的情况，进行扫描
     * 扫描规则：判断已经确认成功付款，但是罚单信息中为未缴费或者缴费失败的记录。完成定时更新，每天晚上执行一次
     *
     * @Scheduled(cron = "0/50 * * * * ?")
     */
    @Lock("retryPayBySuccess")
    @Scheduled(cron = "0 0/30 * * * ?")
    @Transactional(propagation = Propagation.NOT_SUPPORTED, rollbackFor = Exception.class)
    public void retryPayBySuccess() throws Exception {
        //重发回调失败的任务
        List<PayDetail> payDetails = DefaultDSLQueryService
                .createQuery(payDetailDao)
                .where("callbackStatus", "-1")
                .listNoPaging();
        log.info("获取到需要重试回调业务系统数据数量:{}", payDetails.size());
        payDetails.forEach(this::retryCallback);

        List<Runnable> jobs = new ArrayList<>();

        //获取支付状态为0，但是有可能没有收到工行到回调的订单
        try {
            /*
              select * from p_pay_detail
              where
              channel_id='icbc'
              and
              pay_serial_id not in (
                select pay_serial_id from p_pay_detail
                    where
                    pay_status not in('0','-1')
                     and
                    channel_id='icbc'
              )
              and create_time < date_sub(now(), interval 30 MINUTE)
              order by create_time desc
            * */
            DefaultDSLQueryService
                    .createQuery(payDetailDao)
                    .where("channelId", RexPayChannel.icbc)
                    .sql("pay_serial_id not in (select pay_serial_id from p_pay_detail where pay_status not in (?,?) and channel_id='icbc')"
                            , pay_status_new
                            , pay_status_invalidate)
                    .is("payStatus", pay_status_new)
                    //30分钟前的订单
                    .lt("createTime", DateTime.now().plusMinutes(-30).toDate())
                    .orderByDesc("createTime")
                    .list(0, 200)
                    .stream()
                    .collect(Collectors.groupingBy(PayDetail::getPaySerialId)) //按订单号分组
                    .values()
                    .forEach(list -> {
                        try {
                            //取创建时间最新的一条数据
                            list.sort(Comparator.comparing(PayDetail::getCreateTime));
                            PayDetail payDetail = list.get(list.size() - 1);
                            String jsonDetail = JSON.toJSONString(payDetail, SerializerFeature.PrettyFormat);

                            //更新订单号相同的其他订单为-1。说明其他是重复的订单
                            log.info("尝试获取订单支付信息:\n{}\n重复数据数量:{}", jsonDetail, list.size() - 1);
                            Runnable updateRepetition = () -> {
                                if (list.size() == 1) {
                                    return;
                                }
                                List<String> ids = list.stream().map(PayDetail::getId)
                                        .filter(id -> !payDetail.getId().equals(id))
                                        .collect(Collectors.toList());
                                if (!ids.isEmpty()) {
                                    DefaultDSLUpdateService.createUpdate(payDetailDao)
                                            .set("payStatus", pay_status_invalidate)
                                            .set("remark", "已失效")
                                            .where().in("id", ids)
                                            .exec();
                                }
                            };
                            Map<String, String> orderInfoMap;
                            try {
                                orderInfoMap = payDetailService.selectICBCRealPayInfo(payDetail);
                            } catch (BusinessException e) {
                                log.warn("查询工行接口返回:{},\ndata: {}", e.getMessage(), jsonDetail);

                                DefaultDSLUpdateService.createUpdate(payDetailDao)
                                        .set("payStatus", RexPayService.pay_status_invalidate)
                                        .set("remark", e.getMessage() + ":" + ICBCApiRequest.errorMsg.get(e.getMessage()))
                                        .where("id", payDetail.getId())
                                        .exec();
                                updateRepetition.run();
                                return;
                            }

                            log.info("获取到[{}],[{}]实际支付结果:{}", payDetail.getId(), payDetail.getPaySerialId(), orderInfoMap);
                            String tranStat = orderInfoMap.get("tranStat");
                            Map<String, String> param = new HashMap<>();
                            boolean success = "1".equals(tranStat);

                            param.put("comment", orderInfoMap.get("bankRem"));
                            param.put("success", String.valueOf(success));
                            param.put("id", payDetail.getId());

                            PayDetail newDetail = PayDetail.builder()
                                    .payStatus(success ? RexPayService.pay_status_ok : RexPayService.pay_status_fail)
                                    .payReturnTime(new Date())
                                    .payStatusRemark(orderInfoMap.get("bankRem"))
                                    .channelSerialId(orderInfoMap.get("tranSerialNum"))
                                    .build();

                            payDetailService.updateByPk(payDetail.getId(), newDetail);

                            jobs.add(() -> retryCallback(payDetail, param));
                            updateRepetition.run();
                        } catch (Exception e) {
                            log.error("尝试处理未收到工行回调的订单失败", e);
                        }
                    });
            //5秒后发送回调
            executorService.schedule(() -> {
                log.info("开始重试回调,数量:{}", jobs.size());
                jobs.forEach(Runnable::run);
                jobs.clear();
            }, 5, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("尝试处理未收到工行回调的订单失败", e);
            throw e;
        }
    }

}
