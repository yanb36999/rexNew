package com.zmcsoft.rex.commons.district.controller;

import com.zmcsoft.rex.commons.district.api.entity.Road;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import com.zmcsoft.rex.commons.district.api.entity.RoadSeg;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.logging.AccessLogger;
import com.zmcsoft.rex.commons.district.api.service.RoadSegService;
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
@RequestMapping("road-seg")
@Authorize(permission = "district")
@AccessLogger("道路信息")
public class RoadSegController implements SimpleGenericEntityController<RoadSeg, String, QueryParamEntity> {

    private RoadSegService roadSegService;

    @Autowired
    public void setRoadSegService(RoadSegService roadSegService) {
        this.roadSegService = roadSegService;
    }

    @Override
    public RoadSegService getService() {
        return roadSegService;
    }

    @GetMapping("/code/{code}")
    @Authorize(action = Permission.ACTION_QUERY)
    public ResponseMessage<RoadSeg> getByCode(@PathVariable String code) {
        return ResponseMessage.ok(roadSegService.selectByCode(code));
    }
    @GetMapping("/dist-code/{distCode}")
    @Authorize(action = Permission.ACTION_QUERY)
    public ResponseMessage<List<RoadSeg>> getByDistCode(@PathVariable String distCode) {
        return ResponseMessage.ok(roadSegService.selectByDistrictCode(distCode));
    }

    @GetMapping("/road-code/{roadCode}")
    @Authorize(action = Permission.ACTION_QUERY)
    public ResponseMessage<List<RoadSeg>> getByRoleCode(@PathVariable String roadCode) {
        return ResponseMessage.ok(roadSegService.selectByRoadCode(roadCode));
    }
}
