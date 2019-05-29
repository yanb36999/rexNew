package com.zmcsoft.rex.learn.api.entity;
import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.Api;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
/**
* 入城证
* @author hsweb-generator
*/
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(description = "入城证")
public class IntoCityCard extends SimpleGenericEntity<String> {

        //申领成功
        public static final Integer APPLY_OK= 0;
        //申领失败
        public static final Integer APPLY_NO= 1;
        //审核中
        public static final Integer APPLY_ING= 2;
        //申请超时
        public static final Integer APPLY_OVERTIME= 3;

        //入城证类型L为H证
        public static final String CARD_TYPE = "L";

        //入城证模版
        public static final String templateURL= "http://file.rex.cdjg.gov.cn:8090/upload/20171226/2206237046909805.jpg";

        //使用规定
        public static final String applicationRegulations = "http://file.rex.cdjg.gov.cn:8090/upload/20171226/2212841559894066.jpg";


  		@ApiModelProperty(value = "用户id")
        private String openId;
  		@ApiModelProperty(value = "车牌号码")
        private String plateNumber;
  		@ApiModelProperty(value = "车牌种类,01大型车，02小型车")
        private String plateType;
  		@ApiModelProperty(value = "车架号")
        private String vin;
  		@ApiModelProperty(value = "入城证类型")
        private String type;
  		@ApiModelProperty(value = "申领年度")
        private String applyYear;
  		@JSONField(format = "yyyy-MM-dd HH:mm:ss")
  		@ApiModelProperty(value = "申领时间")
        private java.util.Date applyTime;
  		@ApiModelProperty(value = "发证时间")
        private java.util.Date sendTime;
  		@ApiModelProperty(value = "申领状态（2：审核中，0：申领成功，1：申领失败）")
        private Integer applyStatus;
  		@ApiModelProperty(value = "失败类型")
        private String errorType;
  		@ApiModelProperty(value = "失败原因")
        private String errorReason;
  		@ApiModelProperty(value = "创建时间")
        private java.util.Date createTime;
  		@ApiModelProperty(value = "创建时间")
        private java.util.Date updateTime;
  		@ApiModelProperty(value = "用户姓名")
        private String userName;
  		@ApiModelProperty(value = "联系电话")
        private String phone;
  		@ApiModelProperty(value = "车主姓名")
        private String carOwner;

  		@ApiModelProperty(value = "备注")
  		private String remark;

  		@ApiModelProperty(value = "入城证号码")
  		private String cardNo;

  		@ApiModelProperty(value = "入城证图片地址")
  		private String cardImgPath;
}