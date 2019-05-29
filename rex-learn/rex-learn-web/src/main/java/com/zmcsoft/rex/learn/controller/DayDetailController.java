package com.zmcsoft.rex.learn.controller;

import io.swagger.annotations.Api;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import com.zmcsoft.rex.learn.api.entity.DayDetail;
import org.hswebframework.web.logging.AccessLogger;
import  com.zmcsoft.rex.learn.api.service.DayDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *  每天学习记录
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("day-detail")
@Authorize(permission = "day-detail",description = "每天学习记录")
@Api(tags = "教育学习-每天学习记录",value = "每天学习记录")
public class DayDetailController implements SimpleGenericEntityController<DayDetail, String, QueryParamEntity> {

    private DayDetailService dayDetailService;
  
    @Autowired
    public void setDayDetailService(DayDetailService dayDetailService) {
        this.dayDetailService = dayDetailService;
    }
  
    @Override
    public DayDetailService getService() {
        return dayDetailService;
    }
}
