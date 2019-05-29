package com.zmcsoft.rex.learn.impl.service;

import com.zmcsoft.rex.learn.impl.dao.CourseTypeDao;
import com.zmcsoft.rex.learn.api.entity.CourseType;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.id.IDGenerator;
import com.zmcsoft.rex.learn.api.service.CourseTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("courseTypeService")
public class LocalCourseTypeService extends GenericEntityService<CourseType, String>
        implements CourseTypeService {
    @Autowired
    private CourseTypeDao courseTypeDao;
   @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }
    @Override
    public CourseTypeDao getDao() {
        return courseTypeDao;
    }

}
