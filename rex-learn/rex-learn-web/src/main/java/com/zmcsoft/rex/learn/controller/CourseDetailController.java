package com.zmcsoft.rex.learn.controller;

import io.swagger.annotations.Api;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import com.zmcsoft.rex.learn.api.entity.CourseDetail;
import org.hswebframework.web.logging.AccessLogger;
import  com.zmcsoft.rex.learn.api.service.CourseDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *  课件学习记录
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("/course-detail")
@Authorize(permission = "course-detail",description = "课件学习记录")
@Api(tags = "教育学习-课件学习记录",value = "课件学习记录")
public class CourseDetailController implements SimpleGenericEntityController<CourseDetail, String, QueryParamEntity> {

    private CourseDetailService courseDetailService;
  
    @Autowired
    public void setCourseDetailService(CourseDetailService courseDetailService) {
        this.courseDetailService = courseDetailService;
    }
  
    @Override
    public CourseDetailService getService() {
        return courseDetailService;
    }
}
