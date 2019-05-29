package com.zmcsoft.rex.commons.district.impl.service;

import com.zmcsoft.rex.commons.district.api.entity.RoadSeg;
import com.zmcsoft.rex.commons.district.impl.dao.RoadDao;
import com.zmcsoft.rex.commons.district.api.entity.Road;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.id.IDGenerator;
import com.zmcsoft.rex.commons.district.api.service.RoadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("roadService")
@CacheConfig(cacheNames = "rex-road")
public class LocalRoadService extends GenericEntityService<Road, String>
        implements RoadService {
    @Autowired
    private RoadDao roadDao;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public RoadDao getDao() {
        return roadDao;
    }

    @Override
    @Cacheable(key = "'code:'+#code")
    public Road selectByCode(String code) {
        return createQuery().where("code", code).single();
    }

    @Override
    @Caching(
            evict = @CacheEvict(key = "'dist-code:'+#entity.districtCode"),
            put = {
                    @CachePut(key = "'code:'+#entity.code"),
                    @CachePut(key = "'id:'+#result")
            }
    )
    public String insert(Road entity) {
        return super.insert(entity);
    }

    @Override
    @Caching(
            evict = @CacheEvict(key = "'dist-code:'+#entity.districtCode"),
            put = {
                    @CachePut(key = "'code:'+#entity.code"),
                    @CachePut(key = "'id:'+#s")
            }
    )
    public int updateByPk(String s, Road entity) {
        return super.updateByPk(s, entity);
    }

    @Override
    @CacheEvict(allEntries = true)
    public int deleteByPk(String s) {
        return super.deleteByPk(s);
    }

    @Override
    @Cacheable(key = "'id:'+#s")
    public Road selectByPk(String s) {
        return super.selectByPk(s);
    }

    @Override
    //@Cacheable(key = "'dist-code:'+#districtCode")
    public List<Road> selectByDistrictCode(String districtCode) {
        return createQuery()
                .where()
                .$like$("districtCode", districtCode)
                .listNoPaging();
    }
}
