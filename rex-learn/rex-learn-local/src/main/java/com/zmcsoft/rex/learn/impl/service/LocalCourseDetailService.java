package com.zmcsoft.rex.learn.impl.service;

import com.zmcsoft.rex.learn.api.entity.CoursewareMaster;
import com.zmcsoft.rex.learn.api.entity.DayDetail;
import com.zmcsoft.rex.learn.api.entity.ReqCourseDetail;
import com.zmcsoft.rex.learn.api.service.CheckUserService;
import com.zmcsoft.rex.learn.api.service.CoursewareMasterService;
import com.zmcsoft.rex.learn.api.service.DayDetailService;
import com.zmcsoft.rex.learn.impl.dao.CourseDetailDao;
import com.zmcsoft.rex.learn.api.entity.CourseDetail;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.hswebframework.web.BusinessException;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.id.IDGenerator;
import com.zmcsoft.rex.learn.api.service.CourseDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.AbstractDocument;
import java.io.File;
import java.util.*;

import static com.zmcsoft.rex.learn.api.entity.ContentMaster.CHECK_CONTENT_ID;
import static com.zmcsoft.rex.learn.api.entity.ContentMaster.CONTENT_ID;
import static com.zmcsoft.rex.learn.api.entity.CourseDetail.COURSE_ING;
import static com.zmcsoft.rex.learn.api.entity.CourseDetail.COURSE_NO;
import static com.zmcsoft.rex.learn.api.entity.CourseDetail.COURSE_OK;
import static com.zmcsoft.rex.learn.api.entity.CoursewareMaster.COURSE_EXAM;
import static com.zmcsoft.rex.learn.api.entity.CoursewareMaster.COURSE_KNOW;
import static com.zmcsoft.rex.learn.api.entity.CoursewareMaster.COURSE_VIDEO;
import static com.zmcsoft.rex.learn.api.entity.DayDetail.DAY_ING;
import static com.zmcsoft.rex.learn.api.entity.LearnTypeMaster.CHECK_LEARN;
import static com.zmcsoft.rex.learn.api.entity.LearnTypeMaster.FULL_LEARN;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("courseDetailService")
@Slf4j(topic = "business.learn")
public class LocalCourseDetailService extends GenericEntityService<CourseDetail, String>
        implements CourseDetailService {
    @Autowired
    private CourseDetailDao courseDetailDao;

    @Autowired
    private DayDetailService dayDetailService;

    @Autowired
    private CheckUserService checkUserService;

    @Autowired
    private CoursewareMasterService coursewareMasterService;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }
    @Override
    public CourseDetailDao getDao() {
        return courseDetailDao;
    }

    @Override
    public CourseDetail queryByUserIdAndCourseId(String userDetailId,String courseMasterId) {
       Objects.requireNonNull(userDetailId,"用户记录Id不能未空");
       Objects.requireNonNull(courseMasterId,"课件Id不能未空");
       return createQuery().where("userDetailId",userDetailId).and("courseId",courseMasterId).single();
    }

    @Override
    public CourseDetail queryByCourseId(String courseId,String userDetailId){
        Objects.requireNonNull(courseId,"课件ID不能为空");
        Objects.requireNonNull(userDetailId,"用户申请表ID不能为空");
        return createQuery().where("courseId",courseId).and("userDetailId",userDetailId).single();
    }

    @Override
    public List<CourseDetail> queryByUserId(String userDetailId ) {
        return createQuery().where("userDetailId",userDetailId).listNoPaging();
    }

    @Override
    public List<CourseDetail> queryByUserIdAndLearnType(String userDetailId, String learnType) {
        List<CourseDetail> courseDetails = null;
        if(learnType.equals(CHECK_LEARN)){
            courseDetails= createQuery().where("userDetailId", userDetailId).and("contentId", CHECK_CONTENT_ID).listNoPaging();
        }else if (learnType.equals(FULL_LEARN)){
            courseDetails= createQuery().where("userDetailId", userDetailId).and("contentId", CONTENT_ID).listNoPaging();
        }

        return courseDetails;
    }

    @Override
    public List<CourseDetail> queryByDayId(String dayDetailId,String userDetailId) {
        Objects.requireNonNull(dayDetailId,"天记录ID不能为空");
        Objects.requireNonNull(userDetailId,"用户ID不能为空");
        return createQuery().where("dayDetailId",dayDetailId).and("userDetailId",userDetailId).listNoPaging();
    }

    @Override
    public List<CourseDetail> queryCourseFinish(String dayDetailId, String userDetailId) {
        return createQuery().where("dayDetailId",dayDetailId)
                .and("userDetailId",userDetailId)
                .and("status",COURSE_OK)
                .listNoPaging();
    }

    @Override
    public Boolean cleanCourseIng(String dayDetailId, String userDetailId) {
        Objects.requireNonNull(userDetailId,"用户ID不能为空");
        Objects.requireNonNull(dayDetailId,"天ID不能为空");
        createDelete().where("dayDetailId", dayDetailId)
                .and("userDetailId", userDetailId)
                .and("status", COURSE_ING).exec();
        return true;
    }

    @Override
    public Map<String,Object> saveCourseDetail(ReqCourseDetail reqCourseDetail) {
        Objects.requireNonNull(reqCourseDetail.getCourseDetail().getCourseId(),"课件Id不能为空");
        //构造课件学习信息

        CourseDetail courseDetail = reqCourseDetail.getCourseDetail();
        courseDetail.setUserDetailId(reqCourseDetail.getUserDetailId());
        courseDetail.setStatus(COURSE_ING);
        if (reqCourseDetail.getLearnType().equals(CHECK_LEARN)){
            courseDetail.setContentId(CHECK_CONTENT_ID);
        }else if (reqCourseDetail.getLearnType().equals(FULL_LEARN)){
            courseDetail.setContentId(CONTENT_ID);
        }
        courseDetail.setLearnDate(new Date());
        courseDetail.setStartTime(new Date());

        Map ids = new HashMap();
        //判断今天是否存在学习记录
        DayDetail nowDayDetail = dayDetailService.queryNowDayDetail(reqCourseDetail.getUserDetailId());

        if (nowDayDetail!=null){
            throw new BusinessException("今天已经提交过学习记录，不能再学习第二天内容");
        }
        //查询该用户是否存在正在学习的天记录
        DayDetail learningDayDetail = dayDetailService.queryLearningDayByUserDetailId(reqCourseDetail.getUserDetailId());

        if(learningDayDetail==null){
            log.debug("用户ID，没有正在学习的天记录",reqCourseDetail.getUserDetailId(),reqCourseDetail.getDayDetailId());
            //查询这个用户学习完成了几天
            Integer dayDetailCount = dayDetailService.countByUserDetailFinish(reqCourseDetail.getUserDetailId());
            //构建一个今天的正在学习记录
            DayDetail dayDetail = DayDetail.builder()
                    .dayNo(dayDetailCount + 1 + "")
                    .userDetailId(reqCourseDetail.getUserDetailId())
                    .status(DAY_ING)
                    .learnDay(new Date())
                    .startTime(new Date()).build();

            if (reqCourseDetail.getLearnType().equals(CHECK_LEARN)){
                dayDetail.setContentMasterId(CHECK_CONTENT_ID);
                dayDetail.setCountTime(10800L);
                checkUserService.updateLearning(reqCourseDetail.getUserDetailId());
            }else if (reqCourseDetail.getLearnType().equals(FULL_LEARN)){
                dayDetail.setContentMasterId(CONTENT_ID);
                dayDetail.setCountTime(10800L);
            }

            String dayDetailId = dayDetailService.insert(dayDetail);
            log.debug("创建用户学习记录,天记录ID = {},用户ID = {}",dayDetailId,dayDetail.getUserDetailId());

            ids.put("dayDetailId",dayDetailId);
            courseDetail.setDayDetailId(dayDetailId);
        }else {
            log.debug("用户ID={},存在真该学习的天记录ID={}",reqCourseDetail.getUserDetailId(),reqCourseDetail.getDayDetailId());
            //如果有正在学习的天学习记录则把当前课件学习的天记录id设置成正在学习中的天
            courseDetail.setDayDetailId(learningDayDetail.getId());
            ids.put("dayDetailId",learningDayDetail.getId());
        }

        //查询当前课件的学习记录
        CourseDetail detail = queryByCourseId(reqCourseDetail.getCourseDetail().getCourseId(), reqCourseDetail.getUserDetailId());

        //如果这个课件有学习记录，则更新课件学习数据
        if (detail!=null){
            log.info("用户ID= {}，学习过课件ID = {}，",reqCourseDetail.getUserDetailId(),reqCourseDetail.getCourseDetail().getCourseId());
            detail.setStatus(DAY_ING);
            //如果是从视频入口上传的证件，则更新视频字段
            if (reqCourseDetail.getType().equals(COURSE_VIDEO)){
                detail.setVideoIdcardImgPath(reqCourseDetail.getCourseDetail().getVideoIdcardImgPath());
                detail.setVideoUserImgPath(reqCourseDetail.getCourseDetail().getVideoUserImgPath());
            }else if (reqCourseDetail.getType().equals(COURSE_EXAM)){
                detail.setExamIdcardImgPath(reqCourseDetail.getCourseDetail().getExamIdcardImgPath());
                detail.setExamUserImgPath(reqCourseDetail.getCourseDetail().getExamUserImgPath());
            }else {
                throw new BusinessException("课件内容ID错误");
            }
            reqCourseDetail.setDayDetailId(detail.getDayDetailId());
            reqCourseDetail.setCourseDetail(detail);
            super.updateByPk(detail.getId(),detail);
            ids.put("courseDetailId ",detail.getId());
        }else {
            log.info("用户ID= {}，没有学习过课件ID = {},",reqCourseDetail.getUserDetailId(),reqCourseDetail.getCourseDetail().getCourseId());
            //如果没有学习过则直接保存该课件信息
            String courseDetailId = super.saveOrUpdate(reqCourseDetail.getCourseDetail());
            ids.put("courseDetailId ",courseDetailId );
        }
        return ids;
    }

    @Override
    public Boolean updateCourseStatus(Integer type, String courseDetailId) {
//        (0:视频,1:知识点,2:考试)
        Objects.requireNonNull(courseDetailId,"课件记录不能为空");
        Objects.requireNonNull(type,"学习内容不能为空");
        int exec = 0;
       if (type==0){
           exec=createUpdate().
                   where("id",courseDetailId)
                   .set("videoStatus",COURSE_OK)
                   .set("endTime",new Date())
                   .exec();
       }else if (type==1){
           exec=createUpdate()
                   .where("id",courseDetailId)
                   .set("knowledgeStatus",COURSE_OK)
                   .set("endTime",new Date())
                   .exec();
       }else if(type==2){
           exec=createUpdate()
                   .where("id",courseDetailId)
                   .set("examStatus",COURSE_OK)
                   .set("endTime",new Date())
                   .exec();
       }
        return exec==1?true:false;
    }

    @Override
    public Boolean courseComplete(String courseDetailId) {
        CourseDetail detail = super.selectByPk(courseDetailId);
        boolean examStatus = detail.getExamStatus().equals(COURSE_OK);
        boolean knowledgeStatus = detail.getKnowledgeStatus().equals(COURSE_OK);
        boolean videoStatus = detail.getKnowledgeStatus().equals(COURSE_OK);
        CoursewareMaster coursewareMaster = coursewareMasterService.selectByPk(detail.getCourseId());
        int exec = 0;
        if (examStatus&&knowledgeStatus&&videoStatus){
            exec = createUpdate().where("id", courseDetailId).set("status", COURSE_OK).exec();
            if (exec==1){
                //如果课件信息更新成功的话，则这天学习记录时间加上该课件的总时长
                DayDetail dayDetail = dayDetailService.selectByPk(detail.getDayDetailId());
                Long currTime = dayDetail.getCurrTime();
                Long courseMinTime = coursewareMaster.getCourseMinTime();
                Long currTimeCount = currTime+courseMinTime;
              //  Long countTime = dayDetail.getCountTime();
                log.info("提交课件计时:currTime={},courseMinTime={},currTimeCount= {}",currTime,courseMinTime,currTimeCount);
//                //每天总时长只能是14400L
//                if (currTimeCount>countTime){
//                    currTimeCount = countTime;
//                }
                dayDetail.setCurrTime(currTimeCount);
                dayDetailService.saveOrUpdate(dayDetail);
            }else {
                throw new BusinessException("提交数据失败，请重试");
            }
        }else {
            throw new BusinessException("学习未完成,不能提交");
        }
        return true;
    }

    @Override
    public Boolean clearCourseDetail(String userDetailId) {
        Objects.requireNonNull(userDetailId,"用户ID不能为空");
        int courseDetailNum = createQuery().where("userDetailId", userDetailId).total();
        int size = coursewareMasterService.select().size();
        if (courseDetailNum<size){
            throw new BusinessException("课件未学完。不能重置学习记录");
        }
        int exec = createUpdate()
                .where("userDetailId", userDetailId)
                .set("status", COURSE_NO)
                .set("knowledgeStatus", COURSE_NO)
                .set("examStatus", COURSE_NO)
                .set("videoStatus", COURSE_NO)
                .exec();
        log.info("用户Id={},重置课件学习记录数量={},为未完成！",userDetailId,exec);
        return true;
    }
}