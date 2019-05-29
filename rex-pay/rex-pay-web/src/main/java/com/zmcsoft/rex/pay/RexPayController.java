package com.zmcsoft.rex.pay;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/pay")
@Api("支付")
@Slf4j
public class RexPayController {

    @Autowired
    private RexPayService payService;

    @PostMapping("/{channel}")
    @ApiOperation("发起支付请求")
    public ResponseMessage<PayResponse> requestPay(
            @ApiParam("支付渠道,如:icbc")
            @PathVariable String channel,
            @ApiParam("请求参数,不同渠道参数可能不同,请参考稳定")
            @RequestParam Map<String, String> param) {
        log.info("pay start,param:{},channel:{}", JSONObject.toJSONString(param),channel);
        ResponseMessage<PayResponse> responseMessage= ResponseMessage.ok(payService.requestPay(channel, new PayRequest(param)));
        log.info("pay end,responseMessage:{}", JSONObject.toJSONString(responseMessage));
        return responseMessage;
    }

    @RequestMapping("/{channel}/callback")
    @ApiOperation("发起支付请求")
    public ResponseEntity<String> callback(
            @ApiParam("支付渠道,如:icbc")
            @PathVariable String channel,
            @RequestParam Map<String, String> param) {
        log.info("pay channel callback start,param:{},channel:{}", JSONObject.toJSONString(param),channel);
        ResponseEntity<String> responseMessage = ResponseEntity.ok(payService.callback(channel, new PayRequest(param)));
        log.info("pay channel callback end ,responseMessage:{}", JSONObject.toJSONString(responseMessage));
        return responseMessage;
    }

    @PostMapping("/{id}/repeat")
    @ApiOperation("标记为重复缴费")
    @Authorize
    public ResponseEntity<Boolean> markRepeatPay(@PathVariable String id) {
        log.info("pay id repeat start,id:{}",id);
        ResponseEntity<Boolean> responseEntity = ResponseEntity.ok(payService.markRepeatPay(id));
        log.info("pay id repeat end ,responseMessage:{}", JSONObject.toJSONString(responseEntity));
        return responseEntity;
    }
}
