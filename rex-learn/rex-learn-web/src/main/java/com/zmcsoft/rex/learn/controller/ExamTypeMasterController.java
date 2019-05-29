package com.zmcsoft.rex.learn.controller;

import io.swagger.annotations.Api;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import com.zmcsoft.rex.learn.api.entity.ExamTypeMaster;
import org.hswebframework.web.logging.AccessLogger;
import  com.zmcsoft.rex.learn.api.service.ExamTypeMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *  考试类型
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("exam-type-master")
@Authorize(permission = "exam-type-master",description = "考试类型")
@Api(tags = "教育学习-考试类型",value = "考试类型")
public class ExamTypeMasterController implements SimpleGenericEntityController<ExamTypeMaster, String, QueryParamEntity> {

    private ExamTypeMasterService examTypeMasterService;
  
    @Autowired
    public void setExamTypeMasterService(ExamTypeMasterService examTypeMasterService) {
        this.examTypeMasterService = examTypeMasterService;
    }
  
    @Override
    public ExamTypeMasterService getService() {
        return examTypeMasterService;
    }
}
