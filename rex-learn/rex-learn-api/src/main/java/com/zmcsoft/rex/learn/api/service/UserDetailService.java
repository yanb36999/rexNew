package com.zmcsoft.rex.learn.api.service;

import com.zmcsoft.rex.learn.api.entity.UserDetail;
import org.hswebframework.web.service.CrudService;

import java.util.List;

/**
 *  用户信息表 服务类
 *
 * @author hsweb-generator-online
 */
public interface UserDetailService extends CrudService<UserDetail, String> {


    UserDetail queryLearningByLicenseNo(String licenseNo);

    List<UserDetail> queryFinishLearnByLicenseNo(String licenseNo);

    Boolean commitUserDetail(String userDetailId,String learnType);

    UserDetail queryByUserNameAndPhone(String userName,String phone);
}
