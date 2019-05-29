package com.zmcsoft.rex.learn.impl.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.zmcsoft.rex.learn.api.entity.*;
import com.zmcsoft.rex.learn.api.service.*;
import com.zmcsoft.rex.learn.impl.dao.CheckUserDao;
import com.zmcsoft.rex.learn.impl.dao.UserDetailDao;
import com.zmcsoft.rex.learn.impl.service.entity.FtpData;
import com.zmcsoft.rex.learn.impl.service.entity.JsonUserDetail;
import com.zmcsoft.rex.message.MessageSenders;
import com.zmcsoft.rex.message.ftp.FTPMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.BusinessException;
import org.hswebframework.web.concurrent.lock.annotation.Lock;
import org.hswebframework.web.service.DefaultDSLQueryService;
import org.hswebframework.web.service.DefaultDSLUpdateService;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.id.IDGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.zmcsoft.rex.learn.api.entity.CheckUser.CHECK_LEARN_OK;
import static com.zmcsoft.rex.learn.api.entity.LearnTypeMaster.CHECK_LEARN;
import static com.zmcsoft.rex.learn.api.entity.LearnTypeMaster.FULL_LEARN;
import static com.zmcsoft.rex.learn.api.entity.UserDetail.LEARN_OK;
import static com.zmcsoft.rex.utils.FileUtils.convertFileToBase64;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("userDetailService")
@Slf4j(topic = "business.study.user")
public class LocalUserDetailService extends GenericEntityService<UserDetail, String>
        implements UserDetailService {
    @Autowired
    private UserDetailDao userDetailDao;

    @Autowired
    private DayDetailService dayDetailService;

    @Autowired
    private CourseDetailService courseDetailService;

    @Autowired
    private CheckUserService checkUserService;

    @Autowired
    private CoursewareMasterService coursewareMasterService;

    @Autowired
    private MessageSenders messageSenders;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }
    @Override
    public UserDetailDao getDao() {
        return userDetailDao;
    }

    @Override
    public UserDetail queryLearningByLicenseNo(String licenseNo) {
        Objects.requireNonNull(licenseNo,"licenseNo不能为空");
        return createQuery().where("licenseNo",licenseNo).not("status",LEARN_OK).single();
    }

    @Override
    public List<UserDetail> queryFinishLearnByLicenseNo(String licenseNo) {
        Objects.requireNonNull(licenseNo,"驾驶证号不能为空");
        return createQuery().where("licenseNo",licenseNo).and("status",LEARN_OK).listNoPaging();
    }

    @Override
    public Boolean commitUserDetail(String userDetailId ,String learnType) {
        List<DayDetail> dayDetails = dayDetailService.queryByUserDetailId(userDetailId);
        dayDetails.forEach(dayDetail -> {
            List<CourseDetail> courseDetails = courseDetailService.queryByDayId(dayDetail.getId(), userDetailId);
            courseDetails.forEach(courseDetail -> {
                try {
                    CoursewareMaster coursewareMaster = coursewareMasterService.selectByPk(courseDetail.getCourseId());
                    courseDetail.setCourseName(coursewareMaster.getName());
                    courseDetail.setCourseCode(coursewareMaster.getCode());
                    courseDetail.setCurrTime(coursewareMaster.getCourseMinTime());
                    courseDetail.setExamIdcardImgPathBase64(convertFileToBase64(courseDetail.getExamIdcardImgPath()));
                    courseDetail.setVideoIdcardImgPathBase64(convertFileToBase64(courseDetail.getVideoIdcardImgPath()));
                    //  courseDetail.setKnowIdcardImgPath(convertFileToBase64(courseDetail.getKnowIdcardImgPath()));
                    courseDetail.setExamUserImgPathBase64(convertFileToBase64(courseDetail.getExamUserImgPath()));
                    courseDetail.setVideoUserImgPathBase64(convertFileToBase64(courseDetail.getVideoUserImgPath()));
                    //  courseDetail.setKnowUserImgPathBase64(convertFileToBase64(courseDetail.getKnowUserImgPath()));
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new BusinessException("图片转换Base64错误");
                }

            });

            dayDetail.setCourseDetailList(courseDetails);
        });

        FtpData build = null;
        String fileName = null;
        FTPMessageSender sender = messageSenders.ftp();
        if (learnType.equals(CHECK_LEARN)){
            CheckUser checkUser = checkUserService.selectByPk(userDetailId);
            checkUser.setDayDetail(dayDetails);
            fileName = "SYXX_"+checkUser.getLicenseNo()+"_"+System.currentTimeMillis()+".rexsyxx";
            //构造报文
            build= FtpData.builder()
                    .message("审验学习所有记录")
                    .result(checkUser)
                    .status("ok")
                    .timestamp(new Date().toString()).build();
            //修改学习状态
            checkUserService.updateLearnStatus(userDetailId);
        }else if (learnType.equals(FULL_LEARN)){
            UserDetail userDetail = createQuery().where("id", userDetailId).single();
            userDetail.setDayDetailList(dayDetails);

            fileName = "MFXX_"+userDetail.getLicenseNo()+"_"+System.currentTimeMillis()+".rexmfxx";
            //构造报文
            build= FtpData.builder()
                    .message("审验学习所有记录")
                    .result(userDetail)
                    .status("ok")
                    .timestamp(new Date().toString()).build();

            //修改用户学习状态
            createUpdate().where("id", userDetailId)
                    .set("status", 2)
                    .set("finishDate", new Date())
                    .exec();
        }

        //String json =JSON.toJSONString(build, SerializerFeature.WriteMapNullValue);
        //准备json到ftp上传队列
        //执行上传
//        try {
//            //将文件写入到本地
//            OutputStream out = new FileOutputStream(new File("/data/rex-learn/" + fileName));
//            out.write(json.getBytes());
//            //处理文本
//            out.flush();
//            out.close();
//            //执行上传
//            messageSenders.ftp("learn").upload("/DataOut/" + fileName, json);
//            boolean send = sender.send();
//        } catch (Exception e) {
//            log.error("上传文件到ftp失败!", e);
//            throw new BusinessException("提交所有学习记录失败", e);
//        }
        return true;
    }

    @Override
    public UserDetail queryByUserNameAndPhone(String parties, String phone) {
        Objects.requireNonNull(parties,"parties不能为空");
        Objects.requireNonNull(phone,"phone不能为空");
        return createQuery().where("parties",parties)
                .and("phone",phone)
                .not("status",LEARN_OK)
                .single();
    }

    @Lock("readUserDetail")
    @Scheduled(cron = "0/10 * * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void readUserDetail(){
        String path = "/DataIn2/";
        FTPMessageSender sender= messageSenders.ftp("learn");
        sender.list(path, fileName -> {
                    try {
                        ByteArrayOutputStream data = new ByteArrayOutputStream();
                        if (fileName.getName().endsWith(".mfxxdj")) {
                            //下载文件
                            messageSenders.ftp("learn").download(path + fileName.getName(), data).send();
                            data.flush();
                            data.close();

                            String json = data.toString();
                            log.debug("解析满分学习登记表信息:{}\n{}", fileName.getName(), json);
                            if (StringUtils.hasText(json)){
                                JsonUserDetail parseObject = JSON.parseObject(json).toJavaObject(JsonUserDetail.class);
                                UserDetail userDetail = new UserDetail();
                                BeanUtils.copyProperties(parseObject,userDetail);
                                Objects.requireNonNull(userDetail.getUserDetailId(),"报文Id为空");
                                UserDetail userDetailId = createQuery().where("userDetailId", userDetail.getUserDetailId()).single();
                                if(userDetailId!=null){
                                    log.error("登记表已存在,id:{},姓名:{}",userDetailId.getUserDetailId(),userDetailId.getParties());
                                }else {
                                    String insert = insert(userDetail);
                                    log.info("增加用户登记信息，id：{}",insert);
                                }
                            }else {
                                log.error("解析满分学习登记表文件{},文本内容为空",fileName.getName());
                            }

                            new File("/data/rex-learn").mkdirs();
//                            将文件写入到本地
                            OutputStream out = new FileOutputStream(new File("/data/rex-learn/" + fileName.getName()));
                            out.write(data.toByteArray());
                            //处理文本
                            out.flush();
                            out.close();
                            //删除ftp上面已经处理了的文本
                            boolean success = messageSenders.ftp("learn").delete(path + fileName.getName()).send();
                            log.debug("删除FTP文件{} {}", path + fileName.getName(), success);
                        }
                    } catch (Exception e) {
                        log.error("解析违法举报响应报文失败", e);
                        throw new RuntimeException(e);
                    }
                }).send();
    }

}
