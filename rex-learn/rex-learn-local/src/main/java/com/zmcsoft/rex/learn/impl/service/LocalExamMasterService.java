package com.zmcsoft.rex.learn.impl.service;

import com.zmcsoft.rex.learn.impl.dao.ExamMasterDao;
import com.zmcsoft.rex.learn.api.entity.ExamMaster;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.id.IDGenerator;
import com.zmcsoft.rex.learn.api.service.ExamMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("examMasterService")
public class LocalExamMasterService extends GenericEntityService<ExamMaster, String>
        implements ExamMasterService {
    @Autowired
    private ExamMasterDao examMasterDao;
   @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }
    @Override
    public ExamMasterDao getDao() {
        return examMasterDao;
    }

}
