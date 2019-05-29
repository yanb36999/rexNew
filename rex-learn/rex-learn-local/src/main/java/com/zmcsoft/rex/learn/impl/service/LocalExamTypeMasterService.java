package com.zmcsoft.rex.learn.impl.service;

import com.zmcsoft.rex.learn.impl.dao.ExamTypeMasterDao;
import com.zmcsoft.rex.learn.api.entity.ExamTypeMaster;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.id.IDGenerator;
import com.zmcsoft.rex.learn.api.service.ExamTypeMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("examTypeMasterService")
public class LocalExamTypeMasterService extends GenericEntityService<ExamTypeMaster, String>
        implements ExamTypeMasterService {
    @Autowired
    private ExamTypeMasterDao examTypeMasterDao;
   @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }
    @Override
    public ExamTypeMasterDao getDao() {
        return examTypeMasterDao;
    }

}
