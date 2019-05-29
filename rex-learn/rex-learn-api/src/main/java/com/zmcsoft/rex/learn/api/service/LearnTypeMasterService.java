package com.zmcsoft.rex.learn.api.service;

import com.zmcsoft.rex.learn.api.entity.LearnTypeMaster;
import org.hswebframework.web.service.CrudService;

/**
 *  学习类型模板 服务类
 *
 * @author hsweb-generator-online
 */
public interface LearnTypeMasterService extends CrudService<LearnTypeMaster, String> {

    LearnTypeMaster queryByLearnType(String learnType);

    Boolean saveLearnTypeData(LearnTypeMaster learnTypeMaster);

    LearnTypeMaster selectDetailByPk(String id);

}