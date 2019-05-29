package com.zmcsoft.rex.learn.api.service;

import com.zmcsoft.rex.learn.api.entity.DayDetail;
import org.hswebframework.web.service.CrudService;

import java.util.List;

/**
 *  每天学习记录 服务类
 *
 * @author hsweb-generator-online
 */
public interface DayDetailService extends CrudService<DayDetail, String> {

    Integer countByUserDetailFinish(String userDetailId);

    List<DayDetail> queryByUserDetailId(String userDetailId);

    DayDetail queryLearningDayByUserDetailId(String userDetailId);

    boolean updateLearnTime(String dayDetailId,String userId);

    DayDetail queryByDayDetailId(String dayDetailId);

    DayDetail queryNowDayDetail(String userDetailId);

    boolean dayDetailFinish(String dayDetailId,String learnType);
}
