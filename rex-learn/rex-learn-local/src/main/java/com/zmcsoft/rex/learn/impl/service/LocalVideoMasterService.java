package com.zmcsoft.rex.learn.impl.service;

import com.zmcsoft.rex.learn.impl.dao.VideoMasterDao;
import com.zmcsoft.rex.learn.api.entity.VideoMaster;
import com.zmcsoft.rex.video.VideoService;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.id.IDGenerator;
import com.zmcsoft.rex.learn.api.service.VideoMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("videoMasterService")
public class LocalVideoMasterService extends GenericEntityService<VideoMaster, String>
        implements VideoMasterService {
    @Autowired
    private VideoMasterDao videoMasterDao;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public VideoMasterDao getDao() {
        return videoMasterDao;
    }

    @Autowired
    private VideoService videoService;

    @Override
    public String insert(VideoMaster entity) {
        String id = super.insert(entity);
        tryConvertVideo(id, entity.getPath());
        return id;
    }

    @Override
    public int updateByPk(String id, VideoMaster entity) {
        VideoMaster old = selectByPk(id);
        if (old != null) {
            super.updateByPk(id, entity);
            if (!old.getPath().equals(entity.getPath())) {
                tryConvertVideo(id, entity.getPath());
            }
        }
        return 1;
    }

    protected void tryConvertVideo(String id, String path) {
        videoService.convertToM3u8(path, newPath -> {
            if (newPath == null) {
                logger.error("转换视频{},{}失败", id, path);
                return;
            }
            boolean success = videoService.testFile(newPath);
            if (success) {
                createUpdate().set("convertPath", newPath).where("id", id).exec();
            }
        });
    }
}
