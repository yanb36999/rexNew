package com.zmcsoft.rex.learn.impl.service;

import com.zmcsoft.rex.learn.impl.dao.KnowledgeMasterDao;
import com.zmcsoft.rex.learn.api.entity.KnowledgeMaster;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.id.IDGenerator;
import com.zmcsoft.rex.learn.api.service.KnowledgeMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("knowledgeMasterService")
public class LocalKnowledgeMasterService extends GenericEntityService<KnowledgeMaster, String>
        implements KnowledgeMasterService {
    @Autowired
    private KnowledgeMasterDao knowledgeMasterDao;
   @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }
    @Override
    public KnowledgeMasterDao getDao() {
        return knowledgeMasterDao;
    }

}
