package com.zmcsoft.rex.learn.impl.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.zmcsoft.rex.learn.api.CheckStudyStatus;
import com.zmcsoft.rex.learn.api.entity.UserDetail;
import com.zmcsoft.rex.learn.impl.dao.CheckUserDao;
import com.zmcsoft.rex.learn.api.entity.CheckUser;
import com.zmcsoft.rex.learn.impl.service.entity.JsonCheckUser;
import com.zmcsoft.rex.learn.impl.service.entity.JsonUserDetail;
import com.zmcsoft.rex.message.MessageSenders;
import com.zmcsoft.rex.message.ftp.FTPMessageSender;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.core.dsl.Query;
import org.hswebframework.web.BusinessException;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.concurrent.lock.annotation.Lock;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.id.IDGenerator;
import com.zmcsoft.rex.learn.api.service.CheckUserService;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.zmcsoft.rex.learn.api.entity.CheckUser.CHECK_LEARN_ING;
import static com.zmcsoft.rex.learn.api.entity.CheckUser.CHECK_LEARN_INVALID;
import static com.zmcsoft.rex.learn.api.entity.CheckUser.CHECK_LEARN_OK;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("checkUserService")
@Slf4j(topic = "business.study.check.user")
public class LocalCheckUserService extends GenericEntityService<CheckUser, String>
        implements CheckUserService {
    @Autowired
    private CheckUserDao checkUserDao;

    @Autowired
    private MessageSenders messageSenders;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public CheckUserDao getDao() {
        return checkUserDao;
    }

    @Override
    public CheckUser queryLearnIng(String licenseNo) {
        Objects.requireNonNull(licenseNo,"驾驶证号不能为空");
        return checkUserDao.queryCheckUserByLicenseNo(licenseNo,Arrays.asList(CHECK_LEARN_INVALID,CHECK_LEARN_OK));
    }

    @Override
    public List<CheckUser> queryLearnEnd(String licenseNo) {
        Objects.requireNonNull(licenseNo,"驾驶证号不能为空");
        return createQuery().where("licenseNo", licenseNo)
                .and("learnStatus", CHECK_LEARN_OK)
                .listNoPaging();
    }

    @Override
    public CheckUser queryByOpenId(String openId) {
        Objects.requireNonNull(openId,"openId不能为空");
        return createQuery().where("openId", openId).single();
    }

    @Override
    public Boolean updateLearnStatus(String userDetailId) {
        Objects.requireNonNull("userDetailId", "用户Id不能为空");
        createUpdate()
                .set("learnStatus", CHECK_LEARN_OK)
                .set("completeTime", new Date())
                .set("updateTime", new Date())
                .set("remark", "学习完成")
                .where("id", userDetailId).exec();
        return true;
    }

    @Override
    public Boolean updateLearning(String userDetailId) {
        Objects.requireNonNull("userDetailId", "用户Id不能为空");
        createUpdate()
                .set("learnStatus", CHECK_LEARN_ING)
                .set("updateTime", new Date())
                .set("remark", "学习中")
                .where("id", userDetailId).exec();
        return true;
    }

    @Override
    public String queryLastCommitTime(String userDetailId) {
        return checkUserDao.queryLastCommitTime(userDetailId);
    }

    @Override
    public CheckUser queryLastCommitDetail(String openId) {
        List<CheckUser> checkUsers = createQuery().where("openId", openId).orderByDesc("completeTime").listNoPaging();
        if (checkUsers.size() > 0) {
            return checkUsers.get(0);
        } else {
            return null;
        }
    }


    @Lock("clearRecord")
    @Scheduled(cron = "0 0 23-23 * * ? ")
    @Transactional(rollbackFor = Exception.class)
    public void clearRecord() {
        List<CheckUser> checkUsers = createQuery()
                .where("returnStatus", CheckStudyStatus.APPLY_OK.code())
                .not("learnStatus", CHECK_LEARN_OK).listNoPaging();
        checkUsers.forEach(checkUser -> {
            DateTime returnTime = new DateTime(checkUser.getReturnTime());
            DateTime nowTime = new DateTime(new Date());
            Period interval = new Period(returnTime, nowTime, PeriodType.days());
            int days = interval.getDays();
            if (days >= 3) {
                log.info("超时作废", checkUser.getName());
                createUpdate()
                        .where("id", checkUser.getId())
                        .set("returnStatus", CheckStudyStatus.CANCEL.code())
                        .set("learnStatus",CHECK_LEARN_INVALID)
                        .exec();
            }
        });

    }

    @Lock("readCheckUser")
    @Scheduled(cron = "0/10 * * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void readCheckUser() {
        log.info("readCheckUser-定时任务开始");
        String path = "/DataIn2/";
        FTPMessageSender sender= messageSenders.ftp("learn");
        sender.list(path, fileName -> {
                    try {
                        ByteArrayOutputStream data = new ByteArrayOutputStream();
                        if (fileName.getName().endsWith(".syxxsqjg")) {
                            //下载文件
                            messageSenders.ftp("learn").download(path + fileName.getName(), data).send();
                            data.flush();
                            data.close();
                            new File("/data/rex-learn").mkdirs();
//                            将文件写入到本地
                            OutputStream out = new FileOutputStream(new File("/data/rex-learn/" + fileName.getName()));
                            out.write(data.toByteArray());
                            //处理文本
                            out.flush();
                            out.close();

                            String json = data.toString();
                            log.debug("readCheckUser-解析审验学习登记表信息:{}\n{}", fileName.getName(), json);
                            if (StringUtils.hasText(json)) {

//                                List<JsonCheckUser> parseObjectList = JSON.parseObject(json).toJavaObject(List.class);
                                List<JsonCheckUser> jsonCheckUsers = JSON.parseArray(json, JsonCheckUser.class);
                                jsonCheckUsers.forEach(parseObject -> {
                                    CheckUser checkUserCopy = new CheckUser();
                                    BeanUtils.copyProperties(parseObject, checkUserCopy);
                                    CheckUser checkUser = selectByPk(checkUserCopy.getId());
                                    if (checkUser == null) {
                                        throw new BusinessException("readCheckUser-数据错误。");
                                    }
                                    CheckUser merge = com.zmcsoft.rex.commons.district.api.utils.BeanUtils.merge(checkUserCopy, checkUser);
                                    super.saveOrUpdate(merge);
                                });
                            } else {
                                log.error("readCheckUser-解析满分学习登记表文件{},文本内容为空", fileName.getName());
                            }

                            //删除ftp上面已经处理了的文本
                            boolean success = messageSenders.ftp("learn").delete(path + fileName.getName()).send();
                            log.debug("readCheckUser-删除FTP文件{} {}", path + fileName.getName(), success);
                        }
                    } catch (Exception e) {
                        log.error("readCheckUser-解析审验学习报文失败", e);
                        throw new RuntimeException(e);
                    }
                }).send();

        log.info("readCheckUser-定时任务结束");
    }
}
