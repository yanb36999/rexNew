package com.zmcsoft.rex.learn.impl.service;

import com.zmcsoft.rex.learn.api.entity.ContentMaster;
import com.zmcsoft.rex.learn.api.entity.CoursewareMaster;
import com.zmcsoft.rex.learn.api.service.CoursewareMasterService;
import com.zmcsoft.rex.learn.impl.dao.ContentMasterDao;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.id.IDGenerator;
import com.zmcsoft.rex.learn.api.service.ContentMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("contentMasterService")
public class LocalContentMasterService extends GenericEntityService<ContentMaster, String>
        implements ContentMasterService {
    @Autowired
    private ContentMasterDao contentMasterDao;

    @Autowired
    private CoursewareMasterService coursewareMasterService;
   @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }
    @Override
    public ContentMasterDao getDao() {
        return contentMasterDao;
    }

    @Override
    public ContentMaster queryByContentMasterId(String contentMasterId) {
        Objects.requireNonNull(contentMasterId);
        ContentMaster contentMaster = super.selectByPk(contentMasterId);
        List<String> courseIdList = contentMaster.getCourseIdList();
        List<CoursewareMaster> coursewareMasters = coursewareMasterService.selectByPk(courseIdList);
        contentMaster.setCoursewareMasterList(coursewareMasters);
        return contentMaster;
    }

    @Override
    public List<ContentMaster> select() {
        List<ContentMaster> contentMasterList = super.select();
        if (contentMasterList==null){
            return null;
        }
        contentMasterList.forEach(contentMaster -> {
            List<String> courseIdList = contentMaster.getCourseIdList();
            List<CoursewareMaster> coursewareMasters = coursewareMasterService.selectByPk(courseIdList);
            contentMaster.setCoursewareMasterList(coursewareMasters);
        });
        return contentMasterList;
    }
}
