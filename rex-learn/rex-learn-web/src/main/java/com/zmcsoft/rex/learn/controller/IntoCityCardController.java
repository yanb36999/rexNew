package com.zmcsoft.rex.learn.controller;

import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import com.zmcsoft.rex.learn.api.entity.IntoCityCard;
import org.hswebframework.web.logging.AccessLogger;
import  com.zmcsoft.rex.learn.api.service.IntoCityCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *  入城证
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("into-city-card}")
@Authorize(permission = "into-city-card")
@AccessLogger("入城证")
public class IntoCityCardController implements SimpleGenericEntityController<IntoCityCard, String, QueryParamEntity> {

    private IntoCityCardService intoCityCardService;
  
    @Autowired
    public void setIntoCityCardService(IntoCityCardService intoCityCardService) {
        this.intoCityCardService = intoCityCardService;
    }
  
    @Override
    public IntoCityCardService getService() {
        return intoCityCardService;
    }


}
