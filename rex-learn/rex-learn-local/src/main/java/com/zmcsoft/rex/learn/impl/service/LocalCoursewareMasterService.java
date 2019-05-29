package com.zmcsoft.rex.learn.impl.service;

import com.zmcsoft.rex.learn.api.entity.ExamMaster;
import com.zmcsoft.rex.learn.api.entity.KnowledgeMaster;
import com.zmcsoft.rex.learn.api.entity.VideoMaster;
import com.zmcsoft.rex.learn.api.service.ExamMasterService;
import com.zmcsoft.rex.learn.api.service.KnowledgeMasterService;
import com.zmcsoft.rex.learn.api.service.VideoMasterService;
import com.zmcsoft.rex.learn.impl.dao.CoursewareMasterDao;
import com.zmcsoft.rex.learn.api.entity.CoursewareMaster;
import org.hswebframework.web.commons.entity.Entity;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.service.EnableCacheGenericEntityService;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.id.IDGenerator;
import com.zmcsoft.rex.learn.api.service.CoursewareMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("coursewareMasterService")
@CacheConfig(cacheNames = "course-ware-master")
public class LocalCoursewareMasterService extends EnableCacheGenericEntityService<CoursewareMaster, String>
        implements CoursewareMasterService {
    @Autowired
    private CoursewareMasterDao coursewareMasterDao;
    @Autowired
    private ExamMasterService examMasterService;
    @Autowired
    private VideoMasterService videoMasterService;
    @Autowired
    private KnowledgeMasterService knowledgeMasterService;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }
    @Override
    public CoursewareMasterDao getDao() {
        return coursewareMasterDao;
    }


    @Override
    public PagerResult<CoursewareMaster> selectPager(Entity param) {
            PagerResult<CoursewareMaster> coursewareMasterList = super.selectPager(param);

            List<CoursewareMaster> data = coursewareMasterList.getData();

            data.forEach(this::setCorusewareMaster);
        return coursewareMasterList;
    }

    @Override
    public List<CoursewareMaster> selectByPk(List<String> id) {
        List<CoursewareMaster> coursewareMasters = super.selectByPk(id);
        coursewareMasters.forEach(this::setCorusewareMaster);
        return coursewareMasters;
    }

    @Override
    public CoursewareMaster selectByPk(String s) {
        return setCorusewareMaster(super.selectByPk(s));
    }

    public CoursewareMaster setCorusewareMaster(CoursewareMaster coursewareMaster){
        if (coursewareMaster==null)return null;
        List<String> examIdList = coursewareMaster.getExamIdList();
        List<String> knowledgeIdList = coursewareMaster.getKnowledgeIdList();
        List<String> videoIdList = coursewareMaster.getVideoIdList();

        List<ExamMaster> examMasters = examMasterService.selectByPk(examIdList);
        List<KnowledgeMaster> knowledgeMasters = knowledgeMasterService.selectByPk(knowledgeIdList);
        List<VideoMaster> videoMasters = videoMasterService.selectByPk(videoIdList);
        coursewareMaster.setExamMasterList(examMasters);
        coursewareMaster.setKnowledgeMasterList(knowledgeMasters);
        coursewareMaster.setVideoMasterList(videoMasters);

        return coursewareMaster;
    }


}
