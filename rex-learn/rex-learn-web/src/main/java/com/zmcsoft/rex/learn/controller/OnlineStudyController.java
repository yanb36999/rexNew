package com.zmcsoft.rex.learn.controller;



import com.alibaba.fastjson.JSONObject;
import com.zmcsoft.rex.api.user.entity.User;
import com.zmcsoft.rex.api.user.entity.UserDriverLicense;
import com.zmcsoft.rex.api.user.service.UserServiceManager;
import com.zmcsoft.rex.learn.api.entity.*;
import com.zmcsoft.rex.learn.api.service.*;
import com.zmcsoft.rex.learn.api.entity.ReqCourseDetail;
import com.zmcsoft.rex.learn.controller.entity.UserLearnHistory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.hswebframework.web.BusinessException;
import org.hswebframework.web.NotFoundException;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.service.file.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import sun.rmi.runtime.Log;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.zmcsoft.rex.learn.api.entity.ContentMaster.CHECK_CONTENT_ID;
import static com.zmcsoft.rex.learn.api.entity.ContentMaster.CONTENT_ID;
import static com.zmcsoft.rex.learn.api.entity.CourseDetail.COURSE_NO;
import static com.zmcsoft.rex.learn.api.entity.CoursewareMaster.COURSE_EXAM;
import static com.zmcsoft.rex.learn.api.entity.CoursewareMaster.COURSE_KNOW;
import static com.zmcsoft.rex.learn.api.entity.CoursewareMaster.COURSE_VIDEO;
import static com.zmcsoft.rex.learn.api.entity.LearnTypeMaster.CHECK_LEARN;
import static com.zmcsoft.rex.learn.api.entity.LearnTypeMaster.FULL_LEARN;

@RestController
@RequestMapping("/online-study")
@Authorize()
@Api(tags = "在线学习API",value = "OnlineStudyApi")
@Slf4j(topic = "business.learn")
public class OnlineStudyController {

    @Autowired
    private UserDetailService userDetailService;

    @Autowired
    private LearnTypeMasterService learnTypeMasterService;

    @Autowired
    private ContentMasterService contentMasterService;

    @Autowired
    private DayDetailService dayDetailService;

    @Autowired
    private CoursewareMasterService coursewareMasterService;

    @Autowired
    private CourseDetailService courseDetailService;

    @Autowired
    private ExamMasterService examMasterService;

    @Autowired
    private VideoMasterService videoMasterService;

    @Autowired
    private KnowledgeMasterService knowledgeMasterService;

    @Autowired
    private WxMpService wxMpService;

    @Autowired
    private CheckUserService checkUserService;

    @Autowired
    private FileService fileService;

    @Autowired
    private UserServiceManager userServiceManager;

    static  String[] days = {"第0天","第一天","第二天","第三天","第四天","第五天","第六天","第七天","第八天","第九天","第十天","第十一天",};

      private String convertDay(int day){
          if(days.length>=day){
              return days[day];
          }
          return "第"+day+"天";
      }
    @GetMapping("/driver-license")
    @ApiOperation("查询驾驶证数据")
    public ResponseMessage<UserDriverLicense> licenseInfoByCwt(Authentication authentication){
        UserDriverLicense driverLicense = userServiceManager.userDriverLicenseService().getByUserId(authentication.getUser().getId());
        return ResponseMessage.ok(driverLicense);
    }

    @GetMapping("/user-detail")
    @ApiOperation("根据当前登录信息获取用户教育学习详情信息")
    public ResponseMessage<UserDetail> userDetail(Authentication authentication){
        log.info("user-detail start,id:{},name:{}",authentication.getUser().getId(),authentication.getUser().getName());
        User user = userServiceManager.userService().getById(authentication.getUser().getId());
        UserDriverLicense driverLicense = userServiceManager.userDriverLicenseService().getByUserId(authentication.getUser().getId());

        //根据驾驶证信息获取登记表信息
        if (user==null){
            return ResponseMessage.error(403,"该用户未在蓉E行注册");
        }
        //根据姓名和手机号查询满分学习登记表信息
        UserDetail userDetail = userDetailService.queryByUserNameAndPhone(user.getName(),user.getPhone());
        if (userDetail==null){
            log.error("未查到有效的满分学习登记记录。用户姓名：{},联系电话：{}",user.getName(),user.getPhone());
            return ResponseMessage.error(404,"未查到有效的满分学习登记记录");
        }
        log.info("user-detail queryByUserNameAndPhone,userDetail:{}", JSONObject.toJSONString(userDetail));
        if (userDetail.getSendOffice()!=null
                &&userDetail.getSendOffice().equals("川A")
                &&driverLicense==null){
            return ResponseMessage.error(405,"未绑定机动车驾驶证");
        }
        List<DayDetail> dayDetailList = dayDetailService.queryByUserDetailId(userDetail.getId());
        log.info("user-detail queryByUserDetailId,dayDetailList:{}", JSONObject.toJSONString(dayDetailList));
        dayDetailList.forEach(dayDetail -> {
            Integer dayNo = Integer.parseInt(dayDetail.getDayNo());
            String day = convertDay(dayNo);
            dayDetail.setDayNo(day);
        });
        //未学习记录
        int daySum = userDetail.getOnlineLearnDaySum() - dayDetailList.size();
        if (daySum>0){
            for (int i = dayDetailList.size()+1; i <=userDetail.getOnlineLearnDaySum() ; i++) {
                DayDetail dayDetail = new DayDetail();
                dayDetail.setDayNo(convertDay(i));//第几天
//                if (learnType.equals(LearnTypeMaster.FULL_LEARN)){}
                //14400L
                dayDetail.setCountTime(10800L);
                dayDetail.setStatus(0);//状态未未学习
                dayDetailList.add(dayDetail);
            }
        }
        userDetail.setDayDetailList(dayDetailList);
        ResponseMessage<UserDetail> res = ResponseMessage.ok(userDetail);
        log.info("user-detail end,res:{}", res);
        return res;
    }

    @GetMapping("/course-detail/")
    @ApiOperation("根据用户id和天学习记录id获取学习课件列表,如果这天没有学习记录，则不传记录天Id（LeranType:2满分学习。3审验学习）")
    public ResponseMessage<DayDetail> courseDetail(@RequestParam(value = "userDetailId")String userDetailId,
                                                   @RequestParam(value = "dayDetailId",required = false) String dayDetailId,
                                                   @RequestParam(value = "learnType")String learnType){

        log.info("course-detail start,userDetailId:{},dayDetailId:{},learnType:{}",userDetailId,dayDetailId,learnType);
        DayDetail dayDetail = new DayDetail();
        if (dayDetailId!=null){
            dayDetail = dayDetailService.selectByPk(dayDetailId);
        }
        LearnTypeMaster learnTypeMaster = learnTypeMasterService.queryByLearnType(learnType);
        //获取学习内容(原设计的学习天内容)
        ContentMaster contentMaster = contentMasterService.selectByPk(learnTypeMaster.getContentMasterId());
        dayDetail.setContentMasterId(learnTypeMaster.getContentMasterId());
        //获取所有课件集合
        List<CoursewareMaster> coursewareMasters = coursewareMasterService.selectByPk(contentMaster.getCourseIdList());


        List<CourseDetail> courseDetails = courseDetailService.queryByUserIdAndLearnType(userDetailId,learnType);

        Map<String ,CourseDetail> detailCache = courseDetails.stream()
                .collect(Collectors.toMap(CourseDetail::getCourseId,Function.identity()));

        coursewareMasters.forEach(master->{
            CourseDetail detail= detailCache.get(master.getId());
            if(null!=detail){
                detail.setCoursewareMaster(master);
            }else{
                CourseDetail newDetail =  CourseDetail
                        .builder()
                        .coursewareMaster(master)
                        .currTime(0L)
                        .courseId(master.getId())
                        .status(COURSE_NO)
                        .build();
                courseDetails.add(newDetail);
            }
        });

        if (learnType.equals(LearnTypeMaster.FULL_LEARN)){
            dayDetail.setCountTime(10800L);
        }else if (learnType.equals(LearnTypeMaster.CHECK_LEARN)){
            dayDetail.setCountTime(10800L);
        }

        dayDetail.setCourseDetailList(courseDetails);
        return ResponseMessage.ok(dayDetail);
    }


    @GetMapping("course-detail/single")
    @ApiOperation("根据课件Id和用户课件学习记录Id查询课件详情，如果没有学习过则不传记录Id")
    public ResponseMessage<CourseDetail> courseDetailSingle(@RequestParam(value = "courseDetailId",required = false) String courseDetailId,
                                              @RequestParam(value = "userDetailId",required = false) String userDetailId,
                                              @RequestParam(value = "courseMasterId") String courseMasterId){

        //课件详情
        CoursewareMaster coursewareMaster = coursewareMasterService.selectByPk(courseMasterId);
        //课件学习记录
        CourseDetail detail = courseDetailService.queryByUserIdAndCourseId(userDetailId, courseMasterId);

        //课件详情
        List<String> videoIdList = coursewareMaster.getVideoIdList();
        List<String> knowledgeIdList = coursewareMaster.getKnowledgeIdList();
        List<String> examIdList = coursewareMaster.getExamIdList();
        List<VideoMaster> videoMasters = videoMasterService.selectByPk(videoIdList);
        List<KnowledgeMaster> knowledgeMasters = knowledgeMasterService.selectByPk(knowledgeIdList);
        List<ExamMaster> examMasters = examMasterService.selectByPk(examIdList);
        coursewareMaster.setVideoMasterList(videoMasters);
        coursewareMaster.setKnowledgeMasterList(knowledgeMasters);
        coursewareMaster.setExamMasterList(examMasters);

        if (detail==null){
            detail = new CourseDetail();
        }
        detail.setCoursewareMaster(coursewareMaster);
        detail.setContentId(CONTENT_ID);
        detail.setCourseId(courseMasterId);
        return ResponseMessage.ok(detail);
    }

    private Long unixTime() {
        return System.currentTimeMillis() / 1000;
    }

    @PostMapping("user/course-detail")
    @ApiOperation("创建用户学习记录信息 type值：(1:视频,2:知识点,3:考试)")
    public ResponseMessage<Map<String,Object>> saveCourseDetail(@RequestBody ReqCourseDetail reqCourseDetail){
        log.info("user/course-detail start!reqCourseDetail:{}", JSONObject.toJSONString(reqCourseDetail));
        Objects.requireNonNull(reqCourseDetail.getCourseDetail(),"课件数据不能为空");
        Objects.requireNonNull(reqCourseDetail.getUserDetailId(),"用户Id不能为空");
        Objects.requireNonNull(reqCourseDetail.getType(),"学习类型不能为空");
        log.debug("创建用户学习记录,用户ID={},type={},reqCourseDetail= {}",reqCourseDetail.getUserDetailId(),reqCourseDetail.getType(),reqCourseDetail);

        if (reqCourseDetail.getType().equals(COURSE_VIDEO)){
            log.debug("创建用户学习记录,用户Id={},视频,type={},videoIdCard={}",reqCourseDetail.getUserDetailId(),reqCourseDetail.getType(),reqCourseDetail.getCourseDetail().getVideoIdcardImgPath());

            Assert.hasText(reqCourseDetail.getCourseDetail().getVideoIdcardImgPath(),"视频证件照不能为空");
            Assert.hasText(reqCourseDetail.getCourseDetail().getVideoUserImgPath(),"视频自拍照数据不能为空");
            //身份证图片ID
            String idcardImgId = reqCourseDetail.getCourseDetail().getVideoIdcardImgPath();
            //照片Id
            String userImgId = reqCourseDetail.getCourseDetail().getVideoUserImgPath();
            try {
                File videoIdcardImg = wxMpService.getMaterialService().mediaDownload(idcardImgId);
                File videoUserImg = wxMpService.getMaterialService().mediaDownload(userImgId);
                String videoIdcardPath = fileService.saveStaticFile(new FileInputStream(videoIdcardImg), videoIdcardImg.getName());
                String videoUserImgPath = fileService.saveStaticFile(new FileInputStream(videoUserImg), videoUserImg.getName());
                if(StringUtils.isEmpty(videoIdcardPath.trim()) || StringUtils.isEmpty(videoUserImgPath.trim())){
                    throw new BusinessException("网络超时，请重新上传图片");
                }
                log.debug("videoIdcardPath:{}",videoIdcardPath);
                log.debug("videoUserImgPath:{}",videoUserImgPath);
                reqCourseDetail.getCourseDetail().setVideoIdcardImgPath(videoIdcardPath);
                reqCourseDetail.getCourseDetail().setVideoUserImgPath(videoUserImgPath);
            } catch (Exception e) {
                e.printStackTrace();
                throw new BusinessException("网络超时，请重新上传图片");
            }
        }else if (reqCourseDetail.getType().equals(COURSE_EXAM)){
            log.debug("创建用户学习记录,用户Id={},考试,type={},图片={}",reqCourseDetail.getUserDetailId(),reqCourseDetail.getType(),reqCourseDetail.getCourseDetail().getExamIdcardImgPath());
            Assert.hasText(reqCourseDetail.getCourseDetail().getExamIdcardImgPath(),"考试证件照不能为空");
            Assert.hasText(reqCourseDetail.getCourseDetail().getExamUserImgPath(),"考试自拍照数据不能为空");
            //身份证图片ID
            String examIdcardImgId = reqCourseDetail.getCourseDetail().getExamIdcardImgPath();
            //照片Id
            String examUserImgId = reqCourseDetail.getCourseDetail().getExamUserImgPath();
            try {
                File examIdcardImg = wxMpService.getMaterialService().mediaDownload(examIdcardImgId);
                File examUserImg = wxMpService.getMaterialService().mediaDownload(examUserImgId);
                String examIdcardPath = fileService.saveStaticFile(new FileInputStream(examIdcardImg), examIdcardImg.getName());
                String examUserImgPath = fileService.saveStaticFile(new FileInputStream(examUserImg), examUserImg.getName());
                if(StringUtils.isEmpty(examIdcardPath.trim()) || StringUtils.isEmpty(examUserImgPath.trim())){
                    throw new BusinessException("网络超时，请重新上传图片");
                }
                log.debug("examIdcardPath:{}",examIdcardPath);
                log.debug("examUserImgPath:{}",examUserImgPath);
                reqCourseDetail.getCourseDetail().setExamIdcardImgPath(examIdcardPath);
                reqCourseDetail.getCourseDetail().setExamUserImgPath(examUserImgPath);
            } catch (Exception e) {
                e.printStackTrace();
                throw new BusinessException("网络超时，请重新上传图片");
            }
        }else {
            throw new BusinessException("Type参数错误");
        }

        return ResponseMessage.ok(courseDetailService.saveCourseDetail(reqCourseDetail));
    }

    @GetMapping("/course-detail/single/finish/{type}/{courseDetailId}")
    @ApiOperation("根据学习内容和课件记录Id更新学习状态(0:视频,1:知识点,2:考试)")
    public ResponseMessage<Boolean> courseDetailSingleStatus(@PathVariable Integer type,
                                                             @PathVariable String courseDetailId){
        return ResponseMessage.ok(courseDetailService.updateCourseStatus(type,courseDetailId));
    }

    @GetMapping("/course-detail/complete/{courseDetail}")
    @ApiOperation("根据课件学习记录Id更新学习状态，必须知识点，视频，考试都完成才能更新成功")
    public ResponseMessage<Boolean> courseDetailStatus(@PathVariable String courseDetail){
        return ResponseMessage.ok(courseDetailService.courseComplete(courseDetail));
    }

    @GetMapping("/day-detail/finish")
    @ApiOperation("提交一天的学习记录 learnType = 2 满分学习 =3 审验学习")
    public ResponseMessage<Boolean> dayDetailFinish(String dayDetailId,String learnType){
        log.info("day-detail/finish start! dayDetailId:{},learnType:{}",dayDetailId,learnType);
        return ResponseMessage.ok(dayDetailService.dayDetailFinish(dayDetailId,learnType));
    }

    @GetMapping("/user-detail/commit")
    @ApiOperation("提交学习总记录，learnType = 2 满分学习 =3 审验学习")
    public ResponseMessage<Boolean> commitAllDetail(String userDetailId,String learnType){
        return ResponseMessage.ok(userDetailService.commitUserDetail(userDetailId,learnType));
    }

    @GetMapping("/day-detail/{userDetailId}")
    @ApiOperation("根据用户登记表的Id获取所有学习记录")
    public ResponseMessage<List<DayDetail>> dayDetail(@PathVariable String userDetailId){
        List<DayDetail> dayDetailList = dayDetailService.queryByUserDetailId(userDetailId);
        return ResponseMessage.ok(dayDetailList);
    }


    @GetMapping("/user-detail/history")
    @ApiOperation("用户历史学习记录")
    public ResponseMessage<List<UserDetail>> userDetailHistory(Authentication authentication){
        UserDriverLicense driverLicense = userServiceManager.userDriverLicenseService().getByUserId(authentication.getUser().getId());
        //根据驾驶证信息获取登记表信息
        if (driverLicense==null){
            throw new BusinessException("用户驾驶证无效");
        }
        List<UserDetail> userDetails = userDetailService.queryFinishLearnByLicenseNo(driverLicense.getLicenseNumber());

        userDetails.forEach(userDetail -> {
            List<DayDetail> dayDetailList = dayDetailService.queryByUserDetailId(userDetail.getId());
            userDetail.setDayDetailList(dayDetailList);
        });
        return ResponseMessage.ok(userDetails);

    }

    @GetMapping("/user-detail/all-history")
    @ApiOperation("用户所有历史学习记录（审验学习+满分学习）")
    public ResponseMessage<UserLearnHistory> allDetailHistory(Authentication authentication){
        UserDriverLicense driverLicense = userServiceManager.userDriverLicenseService().getByUserId(authentication.getUser().getId());
        //根据驾驶证信息获取登记表信息
        if (driverLicense==null){
            throw new BusinessException("用户驾驶证无效");
        }
        List<UserDetail> userDetails = userDetailService.queryFinishLearnByLicenseNo(driverLicense.getLicenseNumber());
        List<CheckUser> checkUsers = checkUserService.queryLearnEnd(driverLicense.getLicenseNumber());

        userDetails.forEach(userDetail -> {
            List<DayDetail> dayDetailList = dayDetailService.queryByUserDetailId(userDetail.getId());
            userDetail.setDayDetailList(dayDetailList);
        });
        checkUsers.forEach(userDetail -> {
            List<DayDetail> dayDetailList = dayDetailService.queryByUserDetailId(userDetail.getId());
            userDetail.setDayDetail(dayDetailList);
        });

        UserLearnHistory userLearnHistory = UserLearnHistory.builder()
                .checkUserList(checkUsers)
                .userDetailList(userDetails)
                .build();
        return ResponseMessage.ok(userLearnHistory);

    }


    @GetMapping("clear/course-detail")
    @ApiOperation("清空用户课件学习记录")
    public ResponseMessage<Boolean> clearCourseDetail(String userDetailId){
        return ResponseMessage.ok(courseDetailService.clearCourseDetail(userDetailId));
    }
}
