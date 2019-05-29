package com.zmcsoft.rex.pay.impl.service;

import com.zmcsoft.rex.entity.PayDetail;
import com.zmcsoft.rex.pay.AsyncCallbackSupport;
import com.zmcsoft.rex.pay.icbc.ICBCApiRequest;
import com.zmcsoft.rex.pay.impl.dao.PayDetailDao;
import com.zmcsoft.rex.service.PayDetailService;
import org.dom4j.*;
import org.hswebframework.utils.time.DateFormatter;
import org.hswebframework.web.BusinessException;
import org.hswebframework.web.Maps;
import org.hswebframework.web.dao.CrudDao;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.GenericEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 微信推送消息日志
 *
 * @author zhouhao
 * @since 1.0
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
public class LocalPayDetailService extends GenericEntityService<PayDetail, String> implements PayDetailService {

    private AsyncCallbackSupport asyncCallbackSupport=new AsyncCallbackSupport();

    @Autowired
    private PayDetailDao payDetailDao;

    @Autowired
    private ICBCApiRequest icbcApiRequest;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public CrudDao<PayDetail, String> getDao() {
        return payDetailDao;
    }

    @Override
    public PayDetail selectByPaySerialId(String serId) {
        Objects.requireNonNull(serId);
        return createQuery()
                .where("paySerialId", serId)
                .orderByDesc("createTime")
                .single();
    }

    @Override
    public PayDetail selectByPaySerialIdAndStatusAndChannelId(String serId, String status, String channelId) {
        Objects.requireNonNull(serId);
        Objects.requireNonNull(channelId);
        return createQuery()
                .where("paySerialId", serId)
                .and("payStatus", status)
                .and("channelId", channelId)
                .orderByDesc("createTime")
                .single();
    }

    @Override
    public List<PayDetail> selectByBookDate(String bookDate) {
        Objects.requireNonNull(bookDate);
        //pay_status = '1' and channel_id = 'icbc' and DATE_FORMAT(pay_return_time,'%Y-%m-%d') = #{bookDate}
        return createQuery()
                .where("payStatus", "1")
                .and("channelId", "icbc")
                .sql("DATE_FORMAT(pay_return_time,'%Y-%m-%d') = ?", bookDate)
                .listNoPaging();
        //return payDetailDao.selectByBookDate(bookDate);
    }

    public Map<String, String> selectICBCRealPayInfo( PayDetail detail) {
        try {

            String response = icbcApiRequest.doPost("https://corporbank.icbc.com.cn:446/servlet/ICBCINBSEBusinessServlet",
                    Maps.<String, String>buildMap()
                            .put("APIName", "EAPI")
                            .put("APIVersion", "001.001.002.001")
                            .put("MerReqData", "<?xml  version=\"1.0\" encoding=\"GBK\" standalone=\"no\" ?>" +
                                    "<ICBCAPI>" +
                                    "<in>" +
                                    "<orderNum>" +detail.getPaySerialId() + "</orderNum>" +
                                    "<tranDate>"+(DateFormatter.toString(detail.getCreateTime(),"yyyyMMdd"))+"</tranDate>" +
                                    "<ShopCode>4402EE20210014</ShopCode>" +
                                    "<ShopAccount>4402208011921001241</ShopAccount>" +
                                    "</in>" +
                                    "</ICBCAPI>")
                            .get());
            String errorMsg = ICBCApiRequest.errorMsg.get(response);

            if (errorMsg != null) {
                throw new BusinessException(response);
            }
            Document document = DocumentHelper.parseText(response);
            Element root = document.getRootElement();
            List<Node> out = root.selectNodes("//out/*");

            Map<String, String> orderInfoMap = new HashMap<>();
            for (Node node : out) {
                orderInfoMap.put(node.getName(), node.getText());
            }
            return orderInfoMap;
        } catch (IOException | DocumentException e) {
            throw new RuntimeException(e);
        }

    }
}
