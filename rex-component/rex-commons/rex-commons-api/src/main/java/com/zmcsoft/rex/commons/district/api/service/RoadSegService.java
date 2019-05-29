package com.zmcsoft.rex.commons.district.api.service;

import com.zmcsoft.rex.commons.district.api.entity.RoadSeg;
import org.hswebframework.web.service.CrudService;

import java.util.List;

/**
 *  道路信息 服务类
 *
 * @author hsweb-generator-online
 */
public interface RoadSegService extends CrudService<RoadSeg, String> {

    RoadSeg selectByCode(String code);

    List<RoadSeg> selectByRoadCode(String roadCode);

    List<RoadSeg> selectByDistrictCode(String districtCode);

}
