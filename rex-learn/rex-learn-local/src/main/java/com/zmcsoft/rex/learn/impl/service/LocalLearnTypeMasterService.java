package com.zmcsoft.rex.learn.impl.service;

import com.zmcsoft.rex.learn.api.entity.ContentMaster;
import com.zmcsoft.rex.learn.api.entity.CoursewareMaster;
import com.zmcsoft.rex.learn.api.service.CoursewareMasterService;
import com.zmcsoft.rex.learn.api.service.ContentMasterService;
import com.zmcsoft.rex.learn.impl.dao.LearnTypeMasterDao;
import com.zmcsoft.rex.learn.api.entity.LearnTypeMaster;
import org.apache.commons.collections.CollectionUtils;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.id.IDGenerator;
import com.zmcsoft.rex.learn.api.service.LearnTypeMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("learnTypeMasterService")
public class LocalLearnTypeMasterService extends GenericEntityService<LearnTypeMaster, String>
        implements LearnTypeMasterService {
    @Autowired
    private LearnTypeMasterDao learnTypeMasterDao;

    @Autowired
    private ContentMasterService contentMasterService;

    @Autowired
    private CoursewareMasterService coursewareMasterService;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }
    @Override
    public LearnTypeMasterDao getDao() {
        return learnTypeMasterDao;
    }

    @Override
    public LearnTypeMaster queryByLearnType(String learnTypeId) {
        Objects.requireNonNull(learnTypeId,"学习类型不能为空");

        LearnTypeMaster learnTypeMaster = selectByPk(learnTypeId);
        if(learnTypeMaster==null){
            return null;
        }
        String contentId = learnTypeMaster.getContentMasterId();
        ContentMaster contentMaster =contentMasterService.selectByPk(contentId);

        learnTypeMaster.setContentMaster(contentMaster);
        return learnTypeMaster;
    }


    @Override
    public String saveOrUpdate(LearnTypeMaster entity) {
        //获取学习内容模板详情
        ContentMaster contentMaster = entity.getContentMaster();
        String dayMasterId = contentMasterService.saveOrUpdate(contentMaster);
        //设置学习类型内容模板Id集合
        entity.setContentMasterId(entity.getContentMasterId());
        //保存数据
        return super.saveOrUpdate(entity);
    }


    @Override
    public String insert(LearnTypeMaster entity) {
        //获取学习内容模板详情
        ContentMaster contentMaster = entity.getContentMaster();
        String contentMasterId = contentMasterService.insert(contentMaster);
        //设置学习类型天模板Id集合
        entity.setContentMasterId(contentMasterId);
        //保存数据
        return super.insert(entity);
    }

    @Override
    public Boolean saveLearnTypeData(LearnTypeMaster learnTypeMaster) {
        //获取学习天模板详情
        ContentMaster contentMaster = learnTypeMaster.getContentMaster();
        String dayMasterId = contentMasterService.insert(contentMaster);
        //设置学习类型天模板Id集合
        learnTypeMaster.setContentMasterId(dayMasterId);
        //保存数据
        insert(learnTypeMaster);
        return true;
    }

    @Override
    public LearnTypeMaster selectByPk(String id) {
        Objects.requireNonNull(id);
        LearnTypeMaster learnTypeMaster = super.selectByPk(id);
        if(learnTypeMaster==null){
            return null;
        }
        String contentMasterId = learnTypeMaster.getContentMasterId();
        if(contentMasterId==null){
            return learnTypeMaster;
        }
        ContentMaster contentMaster = contentMasterService.selectByPk(contentMasterId);
        if (contentMaster==null){
            return learnTypeMaster;
        }
        List<String> courseIdList = contentMaster.getCourseIdList();
        List<CoursewareMaster> coursewareMasters = coursewareMasterService.selectByPk(courseIdList);
        contentMaster.setCoursewareMasterList(coursewareMasters);
        learnTypeMaster.setContentMaster(contentMaster);
        return learnTypeMaster;
    }
    @Override
    public LearnTypeMaster selectDetailByPk(String id){
        return selectByPk(id);
    }

}
