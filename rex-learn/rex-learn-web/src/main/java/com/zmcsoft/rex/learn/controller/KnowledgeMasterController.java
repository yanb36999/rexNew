package com.zmcsoft.rex.learn.controller;

import io.swagger.annotations.Api;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import com.zmcsoft.rex.learn.api.entity.KnowledgeMaster;
import org.hswebframework.web.logging.AccessLogger;
import  com.zmcsoft.rex.learn.api.service.KnowledgeMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *  知识点
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("knowledge-master")
@Authorize(permission = "knowledge-master",description = "知识点")
@Api(tags = "教育学习-知识点",value = "知识点")
public class KnowledgeMasterController implements SimpleGenericEntityController<KnowledgeMaster, String, QueryParamEntity> {

    private KnowledgeMasterService knowledgeMasterService;
  
    @Autowired
    public void setKnowledgeMasterService(KnowledgeMasterService knowledgeMasterService) {
        this.knowledgeMasterService = knowledgeMasterService;
    }
  
    @Override
    public KnowledgeMasterService getService() {
        return knowledgeMasterService;
    }
}
