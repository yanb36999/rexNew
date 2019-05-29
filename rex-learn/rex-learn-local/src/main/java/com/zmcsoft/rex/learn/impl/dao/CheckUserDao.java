package com.zmcsoft.rex.learn.impl.dao;

import org.apache.ibatis.annotations.Param;
import org.hswebframework.web.dao.CrudDao;
import com.zmcsoft.rex.learn.api.entity.CheckUser;

import java.util.List;

/**
*  审验学习申请表 DAO接口
*  @author hsweb-generator
 */
public interface CheckUserDao extends CrudDao<CheckUser,String> {

    String queryLastCommitTime(String userDetailId);

    CheckUser queryCheckUserByLicenseNo(@Param("licenseNo") String licenseNo,@Param("list") List<Integer> list);
}
