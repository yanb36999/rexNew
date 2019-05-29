package com.zmcsoft.rex.learn.controller;

import io.swagger.annotations.Api;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import com.zmcsoft.rex.learn.api.entity.UserDetail;
import org.hswebframework.web.logging.AccessLogger;
import  com.zmcsoft.rex.learn.api.service.UserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *  用户信息表
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("/user-detail")
@Authorize(permission = "user-detail")
@Api(tags = "教育学习-用户信息表")
public class UserDetailController implements SimpleGenericEntityController<UserDetail, String, QueryParamEntity> {

    private UserDetailService userDetailService;
  
    @Autowired
    public void setUserDetailService(UserDetailService userDetailService) {
        this.userDetailService = userDetailService;
    }
  
    @Override
    public UserDetailService getService() {
        return userDetailService;
    }
}
