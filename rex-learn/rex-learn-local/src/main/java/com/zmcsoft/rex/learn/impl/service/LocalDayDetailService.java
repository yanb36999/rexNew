package com.zmcsoft.rex.learn.impl.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.zmcsoft.rex.api.user.entity.UserDriverLicense;
import com.zmcsoft.rex.api.user.service.UserServiceManager;
import com.zmcsoft.rex.learn.api.entity.*;
import com.zmcsoft.rex.learn.api.service.*;
import com.zmcsoft.rex.learn.impl.dao.DayDetailDao;
import com.zmcsoft.rex.learn.impl.service.entity.FtpData;
import com.zmcsoft.rex.message.MessageSenders;
import com.zmcsoft.rex.message.ftp.FTPMessageSender;
import com.zmcsoft.rex.utils.FileUtils;
import com.zmcsoft.rex.utils.ZipUtil;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.BusinessException;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.id.IDGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.zmcsoft.rex.learn.api.entity.DayDetail.DAY_ING;
import static com.zmcsoft.rex.learn.api.entity.DayDetail.DAY_OK;
import static com.zmcsoft.rex.learn.api.entity.LearnTypeMaster.CHECK_LEARN;
import static com.zmcsoft.rex.learn.api.entity.LearnTypeMaster.FULL_LEARN;
import static com.zmcsoft.rex.utils.FileUtils.convertFileToBase64;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("dayDetailService")
@Slf4j(topic = "business.learn")
public class LocalDayDetailService extends GenericEntityService<DayDetail, String>
        implements DayDetailService {
    @Autowired
    private DayDetailDao dayDetailDao;

    @Autowired
    private UserServiceManager userServiceManager;

    @Autowired
    private UserDetailService userDetailService;

    @Autowired
    private CourseDetailService courseDetailService;

    @Autowired
    private CheckUserService checkUserService;

    @Autowired
    private CoursewareMasterService coursewareMasterService;

    @Autowired
    private MessageSenders messageSenders;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }
    @Override
    public DayDetailDao getDao() {
        return dayDetailDao;
    }


    @Override
    public Integer countByUserDetailFinish(String userDetailId){
        Objects.requireNonNull(userDetailId,"用户ID不能为空");
        return createQuery().where("userDetailId", userDetailId).and("status","2").total();
    }

    @Override
    public List<DayDetail> queryByUserDetailId(String userDetailId) {
        Objects.requireNonNull(userDetailId,"用户ID不能为空");
        List<DayDetail> details = createQuery()
                .where("userDetailId", userDetailId)
                .listNoPaging();

        details.forEach(dayDetail->{
            List<CourseDetail> courseDetails = courseDetailService.queryByDayId(dayDetail.getId(), userDetailId);
            dayDetail.setCourseDetailList(courseDetails);
        });
        return details;
    }

    @Override
    public DayDetail queryLearningDayByUserDetailId(String userDetailId) {
        Objects.requireNonNull(userDetailId,"用户ID不能为空");
        return createQuery().where("userDetailId",userDetailId).and("status",DAY_ING).single();
    }

    public boolean updateLearnTime(String dayDetailId,String userId){

        DayDetail dayDetail = selectByPk(dayDetailId);
        String userDetailId = dayDetail.getUserDetailId();
        UserDetail userDetail = userDetailService.selectByPk(userDetailId);
        Objects.requireNonNull(dayDetail,"没有本次学习记录");
        if (!userDetail.getId().equals(dayDetail.getUserDetailId())){
            log.error("非法请求,dayDetailId:{},userId:{}",dayDetailId,userId);
            return false;
        }

        Date updateTime = dayDetail.getUpdateTime();
        Long currTime = dayDetail.getCurrTime();
        long time = System.currentTimeMillis() - updateTime.getTime();
        boolean status = time>5*60*1000;
        if (status){
            createUpdate().set("updateTime",new Date()).where("id",dayDetailId).exec();
            log.error("用户userId:{},学习时间:{},上次时间:{},本次时间{}",userId,currTime,updateTime,new Date());
            return false;
        }else {
            long countTime = time / 1000 + dayDetail.getCurrTime();
            log.info("用户userId:{},学习时间:{},上次时间:{},本次时间{}",userId,currTime,updateTime,new Date());
            createUpdate().set("updateTime",new Date()).set("currTime",countTime).where("id",dayDetailId).exec();

        }
        return true;
    }

    @Override
    public DayDetail queryByDayDetailId(String dayDetailId) {
        return createQuery().where("id",dayDetailId).single();
    }

    @Override
    public DayDetail queryNowDayDetail(String userDetailId) {
        Objects.requireNonNull(userDetailId,"用户记录ID不能为空");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String format = sdf.format(new Date());
        return createQuery().where("userDetailId",userDetailId).and("status",DAY_OK).sql("DATE_FORMAT(end_time, '%Y-%m-%d') = ?",format).single();
    }


    @Override
    public boolean dayDetailFinish(String dayDetailId,String learnType) {
        Objects.requireNonNull("dayDetailId","天学习记录不能为空");
        Objects.requireNonNull("learnType","学习类型不能为空");
        DayDetail dayDetail = selectByPk(dayDetailId);
        int exec = 0;
        if (dayDetail.getCurrTime().longValue()>=dayDetail.getCountTime().longValue()){

            //更新天学习记录
            exec= createUpdate().where("id", dayDetailId)
                    .set("status", DAY_OK)
                    .set("endTime",new Date())
                    .exec();
            FTPMessageSender sender = messageSenders.ftp();
            FtpData build = null;
            String fileName = null;
            String fileNoEx = null;
             if (exec==1){
                 String userDetailId = dayDetail.getUserDetailId();
                 List<DayDetail> dayDetailList = new ArrayList<>();
                 //更新后的天学习记录
                 DayDetail nowDayDetail = selectByPk(dayDetailId);
                 //查询这天学习已完成的课件数据
                 List<CourseDetail> courseDetails = courseDetailService.queryCourseFinish(dayDetailId, nowDayDetail.getUserDetailId());
                 courseDetails.forEach(courseDetail -> {
                     try {
                         CoursewareMaster coursewareMaster = coursewareMasterService.selectByPk(courseDetail.getCourseId());
                         courseDetail.setCourseName(coursewareMaster.getName());
                         courseDetail.setCourseCode(coursewareMaster.getCode());
                         courseDetail.setCurrTime(coursewareMaster.getCourseMinTime());
                         courseDetail.setExamIdcardImgPathBase64(convertFileToBase64(courseDetail.getExamIdcardImgPath()));
                         courseDetail.setVideoIdcardImgPathBase64(convertFileToBase64(courseDetail.getVideoIdcardImgPath()));
                         courseDetail.setExamUserImgPathBase64(convertFileToBase64(courseDetail.getExamUserImgPath()));
                         courseDetail.setVideoUserImgPathBase64(convertFileToBase64(courseDetail.getVideoUserImgPath()));
                     } catch (IOException e) {
                         e.printStackTrace();
                         throw new BusinessException("图片转换Base64错误");
                     }
                 });
                 nowDayDetail.setCourseDetailList(courseDetails);
                 nowDayDetail.setCourseCount(courseDetails.size());
                 dayDetailList.add(nowDayDetail);
                 //清除当天学习中的课件
                 courseDetailService.cleanCourseIng(dayDetailId, nowDayDetail.getUserDetailId());
                 if (learnType.equals(FULL_LEARN)){
                     //上传满分学习天学习记录
                     UserDetail userDetail = userDetailService.selectByPk(userDetailId);
                     if (userDetail!=null){
                         userDetail.setDayDetailList(dayDetailList);
                     }else {
                         throw new BusinessException("学习类型错误与用户Id不匹配");
                     }
                     log.info("满分学习记录上传，learnType={},userDetailId={}",learnType,userDetailId);
                     build = FtpData.builder()
                             .message("满分学习一天的记录")
                             .result(userDetail)
                             .status("ok")
                             .timestamp(new Date().toString()).build();
                     fileNoEx = "MFXXYT_"+dayDetail.getUserDetailId()+"_"+System.currentTimeMillis();
                     fileName = fileNoEx+".rexmfxxyt";
                 }else if (learnType.equals(CHECK_LEARN)){
                     CheckUser checkUser = checkUserService.selectByPk(userDetailId);
                     if (checkUser!=null){
                         checkUser.setDayDetail(dayDetailList);
                     }else {
                         throw new BusinessException("学习类型错误");
                     }
                     log.info("审验学习记录上传,learnType={},userDetail={}",learnType,userDetailId);
                     build = FtpData.builder()
                             .message("学习一天的记录")
                             .result(checkUser)
                             .status("ok")
                             .timestamp(new Date().toString()).build();
                     fileNoEx = "SYXXYT_"+dayDetail.getUserDetailId()+"_"+System.currentTimeMillis();
                     fileName = fileNoEx+".resyxxyt";
                 }
             }
            String json = JSON.toJSONString(build, SerializerFeature.WriteMapNullValue);

            //执行上传
            try {
                //将文件写入到本地
                OutputStream out = new FileOutputStream(new File("/data/rex-learn/" + fileName));
                out.write(json.getBytes());
                //处理文本
                out.flush();
                out.close();

                ZipUtil.zipFile(new File("/data/rex-learn/tmp/"+fileNoEx+".zip"),"/data/rex-learn/" + fileName,fileName);
                log.info("zip file name:{}",fileNoEx);
                //准备json到ftp上传队列
                File file = new File("/data/rex-learn/tmp/"+fileNoEx+".zip");
                file.renameTo(new File("/data/rex-learn/tmp/"+fileName));
                sender.upload("/DataOut/" + fileName, FileUtils.getFileInputStream("/data/rex-learn/tmp/"+fileName));
                boolean send = sender.send();
            } catch (Exception e) {
                log.error("上传文件到ftp失败!", e);
                throw new BusinessException("提交一天学习记录失败", e);
            }
        }else {
            throw new BusinessException("学时未达到，不能提交");
        }

        return true;
    }
}
