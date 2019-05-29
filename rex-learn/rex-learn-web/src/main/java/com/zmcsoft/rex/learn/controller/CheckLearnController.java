package com.zmcsoft.rex.learn.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.zmcsoft.rex.api.user.entity.UserDriverLicense;
import com.zmcsoft.rex.api.user.service.UserServiceManager;
import com.zmcsoft.rex.learn.api.CheckStudyStatus;
import com.zmcsoft.rex.learn.api.entity.CheckUser;
import com.zmcsoft.rex.learn.api.entity.DayDetail;
import com.zmcsoft.rex.learn.api.service.CheckUserService;
import com.zmcsoft.rex.learn.api.service.DayDetailService;
import com.zmcsoft.rex.message.MessageSenders;
import com.zmcsoft.rex.message.ftp.FTPMessageSender;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.BusinessException;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.id.IDGenerator;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.IdGenerator;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.zmcsoft.rex.learn.api.entity.CheckUser.CHECK_LEARN_NO;
import static com.zmcsoft.rex.learn.api.entity.ContentMaster.CONTENT_ID;

@RestController
@RequestMapping("/check-study")
@Authorize()
@Api(tags = "审验学习API",value = "CheckStudyApi")
@Slf4j
public class CheckLearnController {

    @Autowired
    private CheckUserService checkUserService;

    @Autowired
    private UserServiceManager userServiceManager;

    @Autowired
    private DayDetailService dayDetailService;

    @Autowired
    private MessageSenders messageSenders;


    @PostMapping("/check-user-detail")
    @ApiOperation("提交审验学习申请")
    public ResponseMessage<Boolean> submitCheckUserDetail(Authentication authentication){
        log.info("check-study check-user-detail start,authentication:{}", JSONObject.toJSONString(authentication));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        CheckUser checkUser = new CheckUser();
        UserDriverLicense driverLicense = userServiceManager.userDriverLicenseService().getByUserId(authentication.getUser().getId());
        checkUser.setOpenId(authentication.getUser().getId());
        checkUser.setName(driverLicense.getDriverName());
        checkUser.setLicenseNo(driverLicense.getLicenseNumber());
        checkUser.setFileNo(driverLicense.getFileNumber());
        checkUser.setSendOffice(driverLicense.getSendOffice());
        checkUser.setDriverType(driverLicense.getDrivingModel());
        checkUser.setPhone(driverLicense.getTelephone());
        checkUser.setLicenseScore(driverLicense.getTotalScore());
        checkUser.setReturnStatus(CheckStudyStatus.REQUEST.code());
        checkUser.setRemark(CheckStudyStatus.REQUEST.message());
        checkUser.setId(IDGenerator.MD5.generate());
        checkUser.setLearnStatus(CHECK_LEARN_NO);
        checkUser.setCountTime("10800");
        checkUser.setCommitTime(sdf.format(new Date()));
        checkUser.setCreateTime(new Date());
        checkUser.setUpdateTime(new Date());

        Objects.requireNonNull(driverLicense.getEndValidDate(),"驾驶证状态错误");
        DateTime validDate = new DateTime(driverLicense.getEndValidDate());
        int monthOfYear = validDate.getMonthOfYear();
        int dayOfMonth = validDate.getDayOfMonth();
        DateTime now = new DateTime(new Date());
        int year = now.getYear();
        DateTime checkDate= new DateTime(year+"-"+monthOfYear+"-"+dayOfMonth);
        Period chcekInterval = new Period(checkDate,now,PeriodType.days());
        int validDay = chcekInterval.getDays();
        if (validDay>30){
            throw new BusinessException("未在您驾驶证审验日期的30日以内提交网上学习申请，请前往交警部门办理！");
        }

        //查询是否存在提交记录
        CheckUser checkUserDetail = checkUserService.queryLastCommitDetail(authentication.getUser().getId());
//        if (checkUserDetail!=null){
//            //如果有记录，则判断
//            Date completeTime = checkUserDetail.getCompleteTime();
//            DateTime completeDate = new DateTime(completeTime);
//            DateTime nowDate = new DateTime(new Date());
//            Period interval = new Period(completeDate,nowDate, PeriodType.days());
//            int days = interval.getDays();
//            if (days<30){
//                throw new BusinessException("您近月才完成过审验学习，必须间隔30天及以上才能重复提交申请学习");
//            }
//        }


        //获取默认的ftp工具
        FTPMessageSender sender = messageSenders.ftp();
        String fileName = "SYXX_"+checkUser.getLicenseNo()+"_"+System.currentTimeMillis()+".rexsyxx";
        String json = JSON.toJSONString(checkUser, SerializerFeature.WriteMapNullValue);
        //准备json到ftp上传队列
        sender.upload("/DataOut/" + fileName, json);
        //执行上传
        try {
            boolean send = sender.send();
            if (send){
                checkUser.setReturnStatus(CheckStudyStatus.COMMIT.code());
                checkUser.setRemark(CheckStudyStatus.COMMIT.message());
                checkUserService.insert(checkUser);
            }
        } catch (Exception e) {
            log.error("上传文件到ftp失败!", e);
            throw new BusinessException("提交审验学习失败，请稍后重试", e);
        }
        log.info("check-study-check-user-detail end!");
        return ResponseMessage.ok(true);
    }

    @GetMapping("/user-detail-status")
    @ApiOperation("获取用户登记详情状态")
    public ResponseMessage<CheckUser> checkUserStatus(Authentication authentication){
        log.info("check-study user-detail-status start,authentication:{}", JSONObject.toJSONString(authentication));
        UserDriverLicense driverLicense = userServiceManager.userDriverLicenseService().getByUserId(authentication.getUser().getId());
        if (driverLicense==null){
            return ResponseMessage.error(405,"没有绑定驾驶证信息");
        }else {
            CheckUser checkUserDetail = checkUserService.queryLearnIng(driverLicense.getLicenseNumber());
            if (checkUserDetail!=null){
                List<DayDetail> dayDetailList = dayDetailService.queryByUserDetailId(checkUserDetail.getId());
                checkUserDetail.setDayDetail(dayDetailList);
            }
            ResponseMessage<CheckUser> responseMessage = ResponseMessage.ok(checkUserDetail);
            log.info("check-study user-detail-status end,responseMessage:{}", JSONObject.toJSONString(responseMessage));
            return responseMessage;
        }
    }


    @GetMapping("/check-user-detail")
    @ApiOperation("获取用户学习数据")
    public ResponseMessage<CheckUser> checkUserDetail(Authentication authentication){
        log.info("check-study check-user-detail start,authentication:{}", JSONObject.toJSONString(authentication));
        UserDriverLicense driverLicense = userServiceManager.userDriverLicenseService().getByUserId(authentication.getUser().getId());
        if (driverLicense==null){
            return ResponseMessage.error(405,"没有绑定驾驶证信息");
        }
        CheckUser checkUser = checkUserService.queryLearnIng(driverLicense.getLicenseNumber());
        Objects.requireNonNull(checkUser.getId(),"该用户未申请学习");
        List<DayDetail> dayDetailList = dayDetailService.queryByUserDetailId(checkUser.getId());
           if (dayDetailList.size()==0){
               DayDetail dayDetail = new DayDetail();
               dayDetail.setLearnDay(new Date());
               dayDetail.setStatus(0);//状态未未学习
               dayDetail.setUpdateTime(new Date());
               dayDetail.setStatus(DayDetail.DAY_NO);
               dayDetail.setUserDetailId(checkUser.getId());
               dayDetailList.add(dayDetail);
           }
           dayDetailList.forEach(dayDetail -> {
               dayDetail.setContentMasterId(CONTENT_ID);
               dayDetail.setDayNo("审验教育学习");
               dayDetail.setCountTime(10800L);
           });
        checkUser.setDayDetail(dayDetailList);
        ResponseMessage<CheckUser> responseMessage = ResponseMessage.ok(checkUser);
        log.info("check-study check-user-detail end,responseMessage:{}", JSONObject.toJSONString(responseMessage));
        return responseMessage;
    }


}
