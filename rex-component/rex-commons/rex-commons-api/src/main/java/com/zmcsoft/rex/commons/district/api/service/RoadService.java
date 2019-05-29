package com.zmcsoft.rex.commons.district.api.service;

import com.zmcsoft.rex.commons.district.api.entity.Road;
import com.zmcsoft.rex.commons.district.api.entity.RoadSeg;
import org.hswebframework.web.service.CrudService;

import java.util.List;

/**
 *  道路信息 服务类
 *
 * @author hsweb-generator-online
 */
public interface RoadService extends CrudService<Road, String> {

    List<Road> selectByDistrictCode(String districtCode);


    Road selectByCode(String code);

}
