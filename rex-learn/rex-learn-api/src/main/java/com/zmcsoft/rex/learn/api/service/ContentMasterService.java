package com.zmcsoft.rex.learn.api.service;

import com.zmcsoft.rex.learn.api.entity.ContentMaster;
import org.hswebframework.web.service.CrudService;

/**
 *  每天学习模板 服务类
 *
 * @author hsweb-generator-online
 */
public interface ContentMasterService extends CrudService<ContentMaster, String> {

    ContentMaster queryByContentMasterId(String contentMasterId);
}
