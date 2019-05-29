package com.zmcsoft.rex.learn.api.service;

import com.zmcsoft.rex.learn.api.entity.CheckUser;
import org.hswebframework.web.service.CrudService;

import java.util.List;

/**
 *  审验学习申请表 服务类
 *
 * @author hsweb-generator-online
 */
public interface CheckUserService extends CrudService<CheckUser, String> {


    CheckUser queryLearnIng(String licenseNo);

    List<CheckUser> queryLearnEnd(String licenseNo);

    CheckUser queryByOpenId(String openId);

    Boolean updateLearnStatus(String userDetailId);

    Boolean updateLearning(String userDetailId);

    String queryLastCommitTime(String userDetailId);

    CheckUser queryLastCommitDetail(String openId);
}
