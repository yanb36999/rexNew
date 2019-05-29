package com.zmcsoft.rex.learn.controller;

import io.swagger.annotations.Api;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import com.zmcsoft.rex.learn.api.entity.CourseType;
import org.hswebframework.web.logging.AccessLogger;
import  com.zmcsoft.rex.learn.api.service.CourseTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *  课件类型
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("/course-type")
@Authorize(permission = "course-type")
@Api(tags = "教育学习-课件类型",value = "课件类型")
public class CourseTypeController implements SimpleGenericEntityController<CourseType, String, QueryParamEntity> {

    private CourseTypeService courseTypeService;
  
    @Autowired
    public void setCourseTypeService(CourseTypeService courseTypeService) {
        this.courseTypeService = courseTypeService;
    }
  
    @Override
    public CourseTypeService getService() {
        return courseTypeService;
    }
}
