package com.zmcsoft.rex.learn.controller;

import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import com.zmcsoft.rex.learn.api.entity.CheckUser;
import org.hswebframework.web.logging.AccessLogger;
import  com.zmcsoft.rex.learn.api.service.CheckUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *  审验学习申请表
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("${hsweb.web.mappings.checkUser:checkUser}")
@Authorize(permission = "checkUser")
@AccessLogger("审验学习申请表")
public class CheckUserController implements SimpleGenericEntityController<CheckUser, String, QueryParamEntity> {

    private CheckUserService checkUserService;
  
    @Autowired
    public void setCheckUserService(CheckUserService checkUserService) {
        this.checkUserService = checkUserService;
    }
  
    @Override
    public CheckUserService getService() {
        return checkUserService;
    }
}
