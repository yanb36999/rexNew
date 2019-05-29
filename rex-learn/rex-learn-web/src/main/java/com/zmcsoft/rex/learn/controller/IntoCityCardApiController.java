package com.zmcsoft.rex.learn.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zmcsoft.rex.api.user.entity.User;
import com.zmcsoft.rex.api.user.service.UserServiceManager;
import com.zmcsoft.rex.learn.api.entity.IntoCityCard;
import com.zmcsoft.rex.learn.api.service.IntoCityCardService;
import com.zmcsoft.rex.message.MessageSenders;
import com.zmcsoft.rex.message.ftp.FTPMessageSender;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.BusinessException;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.id.IDGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.zmcsoft.rex.learn.api.entity.IntoCityCard.APPLY_ING;
import static com.zmcsoft.rex.learn.api.entity.IntoCityCard.CARD_TYPE;

@RestController
@RequestMapping("/online-study")
@Authorize()
@Api(tags = "入城证API", value = "intoCityCardAPI")
@Slf4j
public class IntoCityCardApiController {
    @Autowired
    private MessageSenders messageSenders;

    @Autowired
    private IntoCityCardService intoCityCardService;

    @Autowired
    private UserServiceManager userServiceManager;

    @PostMapping("/apply-into-city-card")
    @ApiOperation("提交申请入城证信息，根据报文需要的字段提交")
    public ResponseMessage<Boolean> commitApply(@RequestBody IntoCityCard intoCityCard, Authentication authentication) {

        log.info("online-study apply-into-city-card start,authentication:{},intoCityCard:{}", JSONObject.toJSONString(authentication),JSONObject.toJSON(intoCityCard));

        Objects.requireNonNull(intoCityCard.getApplyYear(), "申领年度不能为空");
        Objects.requireNonNull(intoCityCard.getPlateType(), "号牌种类不能为空");
        Objects.requireNonNull(intoCityCard.getPlateNumber(), "号牌号码不能为空");
        Objects.requireNonNull(intoCityCard.getVin(), "车架号不能为空");


        //判断是否本人本车，车架号是否正确
//        List<UserCar> userCars = userServiceManager.userCarService().getByUserId(authentication.getUser().getId());
//        String plateNumber = intoCityCard.getPlateNumber();
//        List<String> plateNos = new ArrayList<>();
//        userCars.forEach(car->{
//            plateNos.add(car.getPlateNumber());
//            if (car.getPlateNumber().equals(plateNumber)){
//                if (!intoCityCard.getVin().equals(car.getFrameNumber())){
//                    throw new BusinessException("车架号输入错误");
//                }
//            }
//        });
//        if (!plateNos.contains(plateNumber)){
//            throw new BusinessException("车牌号码错误，请核实申请车辆是否为本人车辆");
//        }

        IntoCityCard applyIng = intoCityCardService.queryApplyIng(intoCityCard.getApplyYear(), intoCityCard.getPlateNumber(),intoCityCard.getPlateType());
        if (applyIng!=null){
            throw new BusinessException("已有正在审核中或者审核成功的申请，请勿重复申请");
        }
        User user = userServiceManager.userService().getById(authentication.getUser().getId());
        //获取默认的ftp工具
        if (user==null){
            throw new BusinessException("当前用户未在蓉E行注册");
        }
        intoCityCard.setOpenId(user.getId());
        intoCityCard.setType(CARD_TYPE);
        intoCityCard.setApplyTime(new Date());
        intoCityCard.setId(IDGenerator.MD5.generate());
        FTPMessageSender sender = messageSenders.ftp("intoCityCard");
        String fileName = "RCZ_" + intoCityCard.getOpenId() + "_" + System.currentTimeMillis() + ".rexrcz";
        String json = JSON.toJSONString(intoCityCard);
        //准备json到ftp上传队列
        sender.upload("/DataOut/" + fileName, json);
        //执行上传
        log.info("上传入城证申请报文，内容:{}",json);
        try {
            boolean send = sender.send();
            if (send) {
                intoCityCard.setCarOwner(user.getName());
                intoCityCard.setUserName(user.getName());
                intoCityCard.setPhone(user.getPhone());
                intoCityCard.setApplyStatus(APPLY_ING);
                String insert = intoCityCardService.insert(intoCityCard);
                log.info("保存申请信息成功{}", insert);
            }
        } catch (Exception e) {
            log.error("上传文件到ftp失败!", e);
            throw new BusinessException("提交入城证申请，请稍后重试", e);
        }
        log.info("online-study apply-into-city-card end");
        return ResponseMessage.ok(true);
    }

    @GetMapping("/apply-into-city-card")
    @ApiOperation("查询所有申请的入城证信息")
    public ResponseMessage<List<IntoCityCard>> applyDetail(Authentication authentication,
                                                           @RequestParam String startTime,
                                                           @RequestParam String endTime) {
        log.info("online-study apply-into-city-card start,authentication:{},startTime:{},endTime:{}", JSONObject.toJSONString(authentication),startTime,endTime);
        List<IntoCityCard> intoCityCards = intoCityCardService.selectByOpenId(authentication.getUser().getId(),startTime,endTime);
        ResponseMessage<List<IntoCityCard>> responseMessage = ResponseMessage.ok(intoCityCards);
        log.info("online-study apply-into-city-card end!,IntoCityCard:{}",JSONObject.toJSONString(responseMessage));
        return responseMessage;
    }

    @GetMapping("/single-card")
    @ApiOperation("根据条件申请的入城证信息")
    public ResponseMessage<IntoCityCard> singleIntoCityCard(Authentication authentication,
                                                            @RequestParam String applyYear,
                                                            @RequestParam String plateNo,
                                                            @RequestParam String plateType,
                                                            @RequestParam String queryType) {
//        IntoCityCard build = IntoCityCard.builder()
//                .applyYear(applyYear)
//                .plateNumber(plateNo)
//                .openId(authentication.getUser().getId())
//                .build();
        log.info("online-study single-card start,authentication:{},applyYear:{},plateNo:{},plateType:{},queryType:{}",
                JSONObject.toJSONString(authentication),applyYear,plateNo,plateType,queryType);

        ResponseMessage<IntoCityCard> responseMessage = null;
        //queryType = 0查询状态=0 的数据,否则查询条件不带状态
        if("0".equals(queryType)){
            responseMessage = ResponseMessage.ok(intoCityCardService
                    .selectSingle(QueryParamEntity.empty()
                            .where("applyYear", applyYear)
                            .and("plateNumber", plateNo)
                            .and("applyStatus", 0)
                            .and("plateType",plateType)));
        }else{
            responseMessage = ResponseMessage.ok(intoCityCardService
                    .selectSingle(QueryParamEntity.empty()
                            .where("applyYear", applyYear)
                            .and("plateNumber", plateNo)
//                            .and("applyStatus", 0)
                            .and("plateType",plateType)));
        }

        log.info("online-study single-card end,responseMessage:{}", JSONObject.toJSONString(responseMessage));
        return responseMessage;
    }



//    @GetMapping("test-get")
//    @ApiOperation("获取测试数据")
//    @Authorize(ignore = true)
//    public ResponseMessage<List<IntoCityCard>> testGet(){
//
//        return ResponseMessage.ok(intoCityCardService.selectByOpenId("o9IjmjrlpvHEuxyIQmcHDOqXy-r0","2011-11-12","2020-11-11"));
//    }
//
//    @PostMapping("test-post")
//    @ApiOperation("测试Post")
//    @Authorize(ignore = true)
//    public ResponseMessage<Boolean> testPost(@RequestBody Login login){
//        System.out.println("username:"+login.getUsername());
//        System.out.println("password:"+login.getPassword());
//        if (login.getUsername().equals("Ling")){
//            return ResponseMessage.ok(true);
//        }else {
//            return ResponseMessage.ok(false);
//        }
//    }

}
