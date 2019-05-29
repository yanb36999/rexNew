package com.zmcsoft.rex.learn.controller;

import io.swagger.annotations.Api;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import com.zmcsoft.rex.learn.api.entity.VideoMaster;
import org.hswebframework.web.logging.AccessLogger;
import  com.zmcsoft.rex.learn.api.service.VideoMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *  视频信息
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("video-master")
@Authorize(permission = "video-master",ignore = true)
@Api(tags = "视频信息")
public class VideoMasterController implements SimpleGenericEntityController<VideoMaster, String, QueryParamEntity> {

    private VideoMasterService videoMasterService;
  
    @Autowired
    public void setVideoMasterService(VideoMasterService videoMasterService) {
        this.videoMasterService = videoMasterService;
    }
  
    @Override
    public VideoMasterService getService() {
        return videoMasterService;
    }

}
