package com.zmcsoft.rex.learn.api.entity;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.Date;
import java.util.List;

/**
* 审验学习申请表
* @author hsweb-generator
*/
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(description = "审验学习申请表")
public class CheckUser extends SimpleGenericEntity<String> {

		//未学习
		public static final Integer CHECK_LEARN_NO= 0;
		//学习中
		public static final Integer CHECK_LEARN_ING= 1;
		//已完成
		public static final Integer CHECK_LEARN_OK=2;
		//超时作废
		public static final Integer CHECK_LEARN_INVALID = 3;

  		@ApiModelProperty(value = "微信用户id")
        private String openId;
  		@ApiModelProperty(value = "申请时间")
        private String commitTime;
  		@ApiModelProperty(value = "反馈时间")
        private java.util.Date returnTime;
  		@ApiModelProperty(value = "反馈状态")
        private String returnStatus;
  		@ApiModelProperty(value = "失败原因标志")
        private String errorSign;
  		@ApiModelProperty(value = "失败原因描述")
        private String errorReason;
  		@ApiModelProperty(value = "姓名")
        private String name;
  		@ApiModelProperty(value = "驾驶证号")
        private String licenseNo;
  		@ApiModelProperty(value = "档案编号")
        private String fileNo;
  		@ApiModelProperty(value = "发证机关")
        private String sendOffice;
  		@ApiModelProperty(value = "准驾车型")
        private String driverType;
  		@ApiModelProperty(value = "联系电话")
        private String phone;
  		@ApiModelProperty(value = "创建时间")
        private java.util.Date createTime;
  		@ApiModelProperty(value = "更新时间")
        private java.util.Date updateTime;
  		@ApiModelProperty(value = "累计记分数")
        private Integer licenseScore;
  		@ApiModelProperty(value = "备注")
        private String remark;
  		@ApiModelProperty(value = "学习状态")
        private Integer learnStatus;
  		@ApiModelProperty(value = "学习时长")
        private String currTime;
  		@ApiModelProperty(value = "完成时间")
  		private Date completeTime;
  		@ApiModelProperty(value = "总共学习时长")
  		private String countTime;
  		@ApiModelProperty(value = "天学习记录")
  		private List<DayDetail> dayDetail;
}