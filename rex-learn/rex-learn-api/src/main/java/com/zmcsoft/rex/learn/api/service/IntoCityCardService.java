package com.zmcsoft.rex.learn.api.service;

import com.zmcsoft.rex.learn.api.entity.IntoCityCard;
import org.hswebframework.web.service.CrudService;

import java.util.List;

/**
 *  入城证 服务类
 *
 * @author hsweb-generator-online
 */
public interface IntoCityCardService extends CrudService<IntoCityCard, String> {


    List<IntoCityCard> selectByOpenId(String openId,String startTime,String endTime);

    IntoCityCard queryApplyIng(String applyYear,String plateNumber,String plateType);
}
