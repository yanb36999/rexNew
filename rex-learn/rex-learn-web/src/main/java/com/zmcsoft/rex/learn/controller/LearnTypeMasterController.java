package com.zmcsoft.rex.learn.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import com.zmcsoft.rex.learn.api.entity.LearnTypeMaster;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.logging.AccessLogger;
import  com.zmcsoft.rex.learn.api.service.LearnTypeMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 *  学习类型模板
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("learn-type-master")
@Authorize(permission = "learn-type-master",description = "学习类型模板")
@Api(tags = "教育学习-学习类型模板",value = "学习类型模板")
public class LearnTypeMasterController implements SimpleGenericEntityController<LearnTypeMaster, String, QueryParamEntity> {

    private LearnTypeMasterService learnTypeMasterService;
  
    @Autowired
    public void setLearnTypeMasterService(LearnTypeMasterService learnTypeMasterService) {
        this.learnTypeMasterService = learnTypeMasterService;
    }
  
    @Override
    public LearnTypeMasterService getService() {
        return learnTypeMasterService;
    }


    @PostMapping("/learn-type-data")
    @ApiOperation("保存学习类型")
    public ResponseMessage<Boolean> learnType(@RequestBody LearnTypeMaster learnTypeMaster){
        return ResponseMessage.ok(learnTypeMasterService.saveLearnTypeData(learnTypeMaster));
    }

    //    @Override
//    public ResponseMessage<LearnTypeMaster> getByPrimaryKey(@PathVariable String id) {
//        return ResponseMessage.ok(learnTypeMasterService.selectDetailByPk(id));
//    }

}
