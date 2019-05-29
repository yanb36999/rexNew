package com.zmcsoft.rex.commons.district.controller;

import com.zmcsoft.rex.commons.district.api.entity.RoadSeg;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import com.zmcsoft.rex.commons.district.api.entity.Road;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.logging.AccessLogger;
import com.zmcsoft.rex.commons.district.api.service.RoadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 道路信息
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("road")
@Authorize(permission = "district")
@AccessLogger("道路信息")
public class RoadController implements SimpleGenericEntityController<Road, String, QueryParamEntity> {

    private RoadService roadService;

    @Autowired
    public void setRoadService(RoadService roadService) {
        this.roadService = roadService;
    }

    @Override
    public RoadService getService() {
        return roadService;
    }

    @GetMapping("/code/{code}")
    @Authorize(action = Permission.ACTION_QUERY)
    public ResponseMessage<Road> getByCode(@PathVariable String code) {
        return ResponseMessage.ok(roadService.selectByCode(code));
    }

    @GetMapping("/dist-code/{distCode}")
    @Authorize(action = Permission.ACTION_QUERY)
    public ResponseMessage<List<Road>> getByDistCode(@PathVariable String distCode) {
        return ResponseMessage.ok(roadService.selectByDistrictCode(distCode));
    }
}
