package com.zmcsoft.rex.pay.impl.dao;

import com.zmcsoft.rex.entity.PayDetail;
import org.hswebframework.web.dao.CrudDao;

import java.util.List;

/**
 * @author zhouhao
 */
public interface PayDetailDao extends CrudDao<PayDetail,String> {

    List<PayDetail> selectRetryPayDetail();

    List<PayDetail>  selectByBookDate(String bookDate);
}
