package com.zmcsoft.rex.learn.controller;

import io.swagger.annotations.Api;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import com.zmcsoft.rex.learn.api.entity.ContentMaster;
import org.hswebframework.web.logging.AccessLogger;
import com.zmcsoft.rex.learn.api.service.ContentMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *  每天学习模板
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("day-master")
@Authorize(permission = "day-master",description = "每天学习模板")
@Api(tags = "教育学习-每天学习模板",value = "每天学习模板")
public class ContentMasterController implements SimpleGenericEntityController<ContentMaster, String, QueryParamEntity> {

    private ContentMasterService dayMasterService;
  
    @Autowired
    public void setDayMasterService(ContentMasterService dayMasterService) {
        this.dayMasterService = dayMasterService;
    }
  
    @Override
    public ContentMasterService getService() {
        return dayMasterService;
    }
}
