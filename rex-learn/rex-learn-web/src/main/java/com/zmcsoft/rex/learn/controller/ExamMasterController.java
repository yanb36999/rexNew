package com.zmcsoft.rex.learn.controller;

import io.swagger.annotations.Api;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import com.zmcsoft.rex.learn.api.entity.ExamMaster;
import org.hswebframework.web.logging.AccessLogger;
import  com.zmcsoft.rex.learn.api.service.ExamMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *  考试题
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("/exam-master")
@Authorize(permission = "exam-master",description ="考试题")
@Api(tags = "教育学习-考试题",value = "考试题")
public class ExamMasterController implements SimpleGenericEntityController<ExamMaster, String, QueryParamEntity> {

    private ExamMasterService examMasterService;
  
    @Autowired
    public void setExamMasterService(ExamMasterService examMasterService) {
        this.examMasterService = examMasterService;
    }
  
    @Override
    public ExamMasterService getService() {
        return examMasterService;
    }
}
