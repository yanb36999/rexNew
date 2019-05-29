package com.zmcsoft.rex.pay;

import com.alibaba.fastjson.JSONObject;
import com.zmcsoft.rex.entity.PayDetail;
import com.zmcsoft.rex.service.PayDetailService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.utils.time.DateFormatter;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pay-detail")
@Authorize(permission = "pay-detail", description = "支付订单详情")
@Api(tags = "支付-订单详情", value = "支付订单详情")
@Slf4j
public class RexPayDetailController implements SimpleGenericEntityController<PayDetail, String, QueryParamEntity> {

    @Autowired
    private PayDetailService payDetailService;

    @Override
    public PayDetailService getService() {
        return payDetailService;
    }

    @GetMapping("/serial/{serialId}")
    @Authorize(action = Permission.ACTION_GET)
    @ApiOperation("根据订单号查询订单详情")
    public ResponseMessage<PayDetail> selectByPaySerialId(@PathVariable String serialId) {
        log.info("pay-detail serial start,serialId:{}", serialId);
        ResponseMessage<PayDetail> responseMessage = ResponseMessage.ok(payDetailService.selectByPaySerialId(serialId));
        log.info("pay-detail serial end,responseMessage:{}", JSONObject.toJSONString(responseMessage));
        return responseMessage;
    }
    @GetMapping("/book-date/{date}")
    @Authorize(action = Permission.ACTION_GET)
    @ApiOperation("查询指定日期的订单")
    public ResponseMessage<List<PayDetail>> selectByBookDate(@ApiParam(value = "日期,格式:yyyy-MM-dd",example = "2017-12-01") @PathVariable String date) {
        log.info("pay-detail book-date start,date:{}", date);
        ResponseMessage<List<PayDetail>> responseMessage = ResponseMessage.ok(payDetailService.selectByBookDate(date));
        log.info("pay-detail book-date end,responseMessage:{}", JSONObject.toJSONString(responseMessage));
        return responseMessage;
    }

    @GetMapping("/icbc-pay-info")
    @Authorize(action = Permission.ACTION_GET)
    @ApiOperation("查询工行实际付款状态")
    public ResponseMessage<Map<String,String>> selectICBCRealPayInfo(@RequestParam String paySerialId,@RequestParam String createTime) {
        log.info("pay-detail icbc-pay-info start,paySerialId:{},createTime:{}", paySerialId,createTime);
        ResponseMessage<Map<String,String>> responseMessage = ResponseMessage.ok(payDetailService.selectICBCRealPayInfo(PayDetail.builder().paySerialId(paySerialId).createTime(DateFormatter.fromString(createTime)).build()));
        log.info("pay-detail icbc-pay-info end,responseMessage:{}", JSONObject.toJSONString(responseMessage));
        return responseMessage;
    }

    @GetMapping("/serial/{serialId}/{status}/{channel}")
    @Authorize(action = Permission.ACTION_GET)
    @ApiOperation("根据订单交易状态以及渠道查询最新的一个订单详情")
    public ResponseMessage<PayDetail> selectByPaySerialIdAndStatusAndChannelId(
            @PathVariable String serialId
            , @PathVariable String status, @PathVariable String channel) {
        log.info("pay-detail serial multi start,serialId:{},status:{},channel:{}", serialId,status,channel);
        ResponseMessage<PayDetail> responseMessage = ResponseMessage.ok(payDetailService.selectByPaySerialIdAndStatusAndChannelId(serialId,status,channel));
        log.info("pay-detail serial multi end,responseMessage:{}", JSONObject.toJSONString(responseMessage));
        return responseMessage;
    }

}
