package com.zmcsoft.rex.learn.api.service;

import com.zmcsoft.rex.learn.api.entity.CourseDetail;
import com.zmcsoft.rex.learn.api.entity.ReqCourseDetail;
import org.hswebframework.web.service.CrudService;

import java.util.List;
import java.util.Map;

/**
 *  课件学习记录 服务类
 *
 * @author hsweb-generator-online
 */
public interface CourseDetailService extends CrudService<CourseDetail, String> {

    CourseDetail queryByUserIdAndCourseId(String userDetailId,String courseDetailId);

    List<CourseDetail> queryByUserId(String userDetailId);

    List<CourseDetail> queryByUserIdAndLearnType(String userDetailId,String contentId);

    CourseDetail queryByCourseId(String courseId,String userDetailId);

    List<CourseDetail> queryByDayId(String dayDetailId,String userDetailId);

    List<CourseDetail> queryCourseFinish(String dayDetailId,String userDetailId);

    Boolean cleanCourseIng(String dayDetailId,String userDetailId);

    Map saveCourseDetail(ReqCourseDetail reqCourseDetail);

    Boolean updateCourseStatus(Integer type,String courseDetailId);

    Boolean courseComplete(String courseDetailId);

    Boolean clearCourseDetail(String userDetailId);
}
