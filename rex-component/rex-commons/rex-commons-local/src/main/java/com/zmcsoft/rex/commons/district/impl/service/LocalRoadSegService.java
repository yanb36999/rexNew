package com.zmcsoft.rex.commons.district.impl.service;

import com.zmcsoft.rex.commons.district.impl.dao.RoadSegDao;
import com.zmcsoft.rex.commons.district.api.entity.RoadSeg;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.id.IDGenerator;
import com.zmcsoft.rex.commons.district.api.service.RoadSegService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("roadSegService")
@CacheConfig(cacheNames = "rex-road-seg")
public class LocalRoadSegService extends GenericEntityService<RoadSeg, String>
        implements RoadSegService {
    @Autowired
    private RoadSegDao roadSegDao;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public RoadSegDao getDao() {
        return roadSegDao;
    }

    @Override
    @Cacheable(key = "'code:'+#code")
    public RoadSeg selectByCode(String code) {
        return createQuery().where("code", code).single();
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(key = "'dist-code:'+#entity.districtCode"),
                    @CacheEvict(key = "'road-code:'+#entity.roadCode"),
            },
            put = {
                    @CachePut(key = "'code:'+#entity.code"),
                    @CachePut(key = "'id:'+#result")
            }
    )
    public String insert(RoadSeg entity) {
        return super.insert(entity);
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(key = "'dist-code:'+#entity.districtCode"),
                    @CacheEvict(key = "'road-code:'+#entity.roadCode"),
            },
            put = {
                    @CachePut(key = "'code:'+#entity.code"),

                    @CachePut(key = "'id:'+#id")
            }
    )
    public int updateByPk(String id, RoadSeg entity) {
        return super.updateByPk(id, entity);
    }

    @Override
    @CacheEvict(allEntries = true)
    public int deleteByPk(String s) {
        return super.deleteByPk(s);
    }

    @Override
    @Cacheable(key = "'id:'+#s")
    public RoadSeg selectByPk(String s) {
        return super.selectByPk(s);
    }

    @Override
    @Cacheable(key = "'dist-code:'+#districtCode")
    public List<RoadSeg> selectByDistrictCode(String districtCode) {
        return createQuery()
                .where("districtCode", districtCode)
                .listNoPaging();
    }

    @Override
    @Cacheable(key = "'road-code:'+#roadCode")
    public List<RoadSeg> selectByRoadCode(String roadCode) {
        return createQuery()
                .where("roadCode", roadCode)
                .listNoPaging();
    }
}
