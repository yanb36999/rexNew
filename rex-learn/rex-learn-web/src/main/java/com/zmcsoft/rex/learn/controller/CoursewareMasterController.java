package com.zmcsoft.rex.learn.controller;

import io.swagger.annotations.Api;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import com.zmcsoft.rex.learn.api.entity.CoursewareMaster;
import org.hswebframework.web.logging.AccessLogger;
import  com.zmcsoft.rex.learn.api.service.CoursewareMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *  课件模板
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("courseware-master")
@Authorize(permission = "courseware-master",description = "课件模板")
@Api(tags = "教育学习-课件模板",value = "课件模板")
public class CoursewareMasterController implements SimpleGenericEntityController<CoursewareMaster, String, QueryParamEntity> {

    private CoursewareMasterService coursewareMasterService;
  
    @Autowired
    public void setCoursewareMasterService(CoursewareMasterService coursewareMasterService) {
        this.coursewareMasterService = coursewareMasterService;
    }
  
    @Override
    public CoursewareMasterService getService() {
        return coursewareMasterService;
    }
}
