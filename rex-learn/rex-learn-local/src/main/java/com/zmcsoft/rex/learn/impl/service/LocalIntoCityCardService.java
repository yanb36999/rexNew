package com.zmcsoft.rex.learn.impl.service;

import com.alibaba.fastjson.JSON;
import com.zmcsoft.rex.learn.impl.dao.IntoCityCardDao;
import com.zmcsoft.rex.learn.api.entity.IntoCityCard;
import com.zmcsoft.rex.learn.impl.service.entity.JsonIntoCityCard;
import com.zmcsoft.rex.message.MessageSenders;
import com.zmcsoft.rex.message.ftp.FTPMessageSender;
import com.zmcsoft.rex.utils.image.ImageLayer;
import com.zmcsoft.rex.utils.image.TextLayer;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.BusinessException;
import org.hswebframework.web.concurrent.lock.annotation.Lock;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.id.IDGenerator;
import com.zmcsoft.rex.learn.api.service.IntoCityCardService;
import org.hswebframework.web.service.file.FileService;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.awt.*;
import java.io.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.zmcsoft.rex.learn.api.entity.IntoCityCard.APPLY_ING;
import static com.zmcsoft.rex.learn.api.entity.IntoCityCard.APPLY_OK;
import static com.zmcsoft.rex.learn.api.entity.IntoCityCard.APPLY_OVERTIME;
import static com.zmcsoft.rex.utils.image.ImageUtils.createBarCode;
import static com.zmcsoft.rex.utils.image.ImageUtils.merge;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("intoCityCardService")
@Slf4j(topic = "business.intoCity")
public class LocalIntoCityCardService extends GenericEntityService<IntoCityCard, String>
        implements IntoCityCardService {

    @Autowired
    private IntoCityCardDao intoCityCardDao;

    @Autowired
    private MessageSenders messageSenders;

    @Autowired
    private FileService fileService;


    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public IntoCityCardDao getDao() {
        return intoCityCardDao;
    }

    @Override
    public List<IntoCityCard> selectByOpenId(String openId,String startTime,String endTime) {
        return createQuery()
                .where("openId", openId)
                .gte("applyTime",startTime)
                .lte("applyTime",endTime)
                .listNoPaging();
    }

    @Override
    public IntoCityCard queryApplyIng(String applyYear, String plateNumber,String plateType) {
        return createQuery()
                .where("applyYear",applyYear)
                .and("plateNumber",plateNumber)
                .in("applyStatus",APPLY_ING,APPLY_OK)
                .and("plateType",plateType)
                .single();
    }

    @Lock("clearIntoCityApplyRecord")
    @Scheduled(cron = "0 0 23-23 * * ? ")
    @Transactional(rollbackFor = Exception.class)
    public void clearIntoCityApplyRecord() {
        log.info("clearIntoCityApplyRecord start!");
        List<IntoCityCard> intoCityCardList = createQuery()
                .where("applyStatus", APPLY_ING).listNoPaging();

        intoCityCardList.forEach(intoCityCard -> {
            DateTime applyTime = new DateTime(intoCityCard.getApplyTime());
            DateTime nowTime = new DateTime(new Date());
            Period interval = new Period(applyTime, nowTime, PeriodType.days());
            int days = interval.getDays();
            if (days >= 1) {
                log.info("超时作废 name:{},id:{}", intoCityCard.getCarOwner(),intoCityCard.getId());
                createUpdate()
                        .where("id", intoCityCard.getId())
                        .set("applyStatus", APPLY_OVERTIME)
                        .set("errorReason","入城证申请超时作废，内网未在24小时内返回报文")
                        .set("remark","入城证申请超时作废，内网未在24小时内返回报文")
                        .exec();
            }
        });
        log.info("clearIntoCityApplyRecord end!");
    }

    @Lock("readIntoCityCard")
    @Scheduled(cron = "0/10 * * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void readIntoCityCard() {
        log.debug("readIntoCityCard-定时任务开始");
        String path = "/DataIn2/";
       // log.info("执行入城证定时任务解析报文");
        messageSenders.ftp("intoCityCard").list(path, fileName -> {
            try {
                ByteArrayOutputStream data = new ByteArrayOutputStream();
                //log.debug("文件名称:{}", fileName.getName());
                if (fileName.getName().endsWith(".rczhzc")) {
                    log.debug("readIntoCityCard-文件名称:{}", fileName.getName());
                    //下载文件
                    boolean downloaded = messageSenders.ftp("intoCityCard")
                            .download(path + fileName.getName(), data)
                            .send();
                    data.flush();
                    data.close();
                    new File("/data/rex-learn").mkdirs();
                    //将文件写入到本地
                    OutputStream out = new FileOutputStream(new File("/data/rex-learn/" + fileName.getName()));
                    out.write(data.toByteArray());
                    //处理文本
                    out.flush();
                    out.close();

                    String json = data.toString();
                    log.debug("readIntoCityCard-入城证申请结果信息:{}\n{}", fileName.getName(), json);
                    if (StringUtils.hasText(json)) {
                        JsonIntoCityCard parseObject = JSON.parseObject(json, JsonIntoCityCard.class);
                       // List<JsonIntoCityCard> intoCityCards = JSON.parseArray(json, JsonIntoCityCard.class);
                        //intoCityCards.forEach(parseObject -> {
                            IntoCityCard intoCityCardCopy = new IntoCityCard();
                            BeanUtils.copyProperties(parseObject, intoCityCardCopy);
                            IntoCityCard intoCityCardOld = selectByPk(intoCityCardCopy.getId());
                            if (intoCityCardOld == null) {
                                boolean success = messageSenders.ftp("intoCityCard").delete(path + fileName.getName()).send();
                                log.error("readIntoCityCard-数据错误。删除错误报文结果：{},内容：{}",success,json);
                                throw new BusinessException("数据错误");
                            }
                            IntoCityCard intoCityCard = com.zmcsoft.rex.utils.BeanUtils.merge(intoCityCardCopy, intoCityCardOld);
                            if (intoCityCard.getApplyStatus().equals(APPLY_OK)){
                                intoCityCard.setCardImgPath(cardImgMerge(intoCityCard));
                            }
                            intoCityCard.setUpdateTime(new Date());
                            super.saveOrUpdate(intoCityCard);

                        //});
                    } else {
                        log.error("readIntoCityCard-解析入城证申请结果文件{},文本内容为空", fileName.getName());
                    }

                    //删除ftp上面已经处理了的文本
                    boolean success = messageSenders.ftp("intoCityCard").delete(path + fileName.getName()).send();
                    log.debug("readIntoCityCard-删除FTP文件{} {}", path + fileName.getName(), success);
                }
            } catch (Exception e) {
                log.error("readIntoCityCard-解析入城证报文失败", e);
                throw new RuntimeException(e);
            }
        }).send();
        log.debug("readIntoCityCard-定时任务结束");
    }


    public String cardImgMerge(IntoCityCard intoCityCard){
        //车牌号
        TextLayer plateNumberLayer = TextLayer.builder()
                .text(removePlateNumber(intoCityCard.getPlateNumber()))
                .x(280).y(350)
                .color(Color.black).font(new Font("Microsoft YaHei", Font.PLAIN, 220))
                .build();
        //条形码
        try {
            try (ByteArrayOutputStream qrcode = new ByteArrayOutputStream()) {
                createBarCode(380, 120, intoCityCard.getCardNo(), qrcode); //生成二维码
                File temp = File.createTempFile("rcz_" + System.currentTimeMillis(), ".jpg");
                try (InputStream template = new FileInputStream("/data/rcz-template.jpg");
                     OutputStream result = new FileOutputStream(temp);
                     InputStream tempStream = new FileInputStream(temp);
                     InputStream qrcodeStream = new ByteArrayInputStream(qrcode.toByteArray())) {
                    ImageLayer qrCodeLayer = ImageLayer.builder()
                            .imageInput(qrcodeStream)
                            .x(5).y(600)
                            .width(350).height(120)
                            .build();
                    //入城证编号
                    TextLayer intoCityCardNoLayer = TextLayer.builder()
                            .text("NO "+intoCityCard.getCardNo())
                            .x(100).y(760)
                            .color(Color.black).font(new Font("Microsoft YaHei", Font.PLAIN, 40))
                            .build();
                    merge(template, result, Arrays.asList(plateNumberLayer, qrCodeLayer, intoCityCardNoLayer));
                    return fileService.saveStaticFile(tempStream, temp.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("入城证合成失败");
        }

    }

    public String removePlateNumber(String in) {
        if (in != null) {
            char c = in.charAt(0);
            if ((c >= 0x4e00) && (c <= 0x9fbb))
                return in.substring(1, in.length());
        }
        return in;
    }
}
