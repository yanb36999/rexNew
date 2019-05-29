package com.zmcsoft.rex.learn.api.entity;
import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.hswebframework.web.validator.group.CreateGroup;

import java.util.Date;
import java.util.List;

/**
* 用户信息表
* @author hsweb-generator
*/
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(description = "用户信息表")
public class UserDetail extends SimpleGenericEntity<String> {

		//未学习
		public static final Integer LEARN_NO= 0;
		//学习中
		public static final Integer LEARN_ING= 1;
		//已完成
		public static final Integer LEARN_OK=2;

  		@ApiModelProperty(value = "学习类型")
		@NotBlank(groups = CreateGroup.class)
        private String type;
  		@ApiModelProperty(value = "学习原因")
        private String reason;
  		@ApiModelProperty(value = "标志")
  		private Integer sign;
  		@ApiModelProperty(value = "学习部门")
        private String dept;
  		@ApiModelProperty(value = "学习部门名称")
        private String deptName;
  		@ApiModelProperty(value = "发证机关")
  		private String sendOffice;
  		@ApiModelProperty(value = "当事人")
		@NotBlank(groups = CreateGroup.class)
        private String parties;
		@ApiModelProperty(value = "内网用户Id")
		@NotBlank(groups = CreateGroup.class)
		private String userDetailId;
		@ApiModelProperty(value = "联系电话")
		@NotBlank(groups = CreateGroup.class)
		private String phone;
  		@ApiModelProperty(value = "驾驶证号")
		@NotBlank(groups = CreateGroup.class)
        private String licenseNo;
  		@ApiModelProperty(value = "档案编号")
        private String fileNo;
  		@ApiModelProperty(value = "驾驶证状态")
        private String licenseStatus;
  		@ApiModelProperty(value = "有效期止")
        private java.util.Date endTime;
  		@ApiModelProperty(value = "累计记分")
        private Integer totalScore;
  		@ApiModelProperty(value = "准驾车型")
        private String driverType;
  		@ApiModelProperty(value = "入库时间")
        private java.util.Date registerTime;
  		@ApiModelProperty(value = "登记人")
        private String registrant;
  		@ApiModelProperty(value = "网上预约学习天数")
        private Integer onlineLearnDaySum;
  		@ApiModelProperty(value = "已学习天数")
        private Integer finishDaySum;
  		@ApiModelProperty(value = "未学习天数")
        private Integer unfinishDaySum;
  		@ApiModelProperty(value = "预约现场学习日期")
        private String offlineLearnTime;
  		@ApiModelProperty(value = "完成日期")
        private Date finishDate;
  		@ApiModelProperty(value = "状态 0：未学习；1：学习中；2：已完成")
        private Integer status;

  		@ApiModelProperty(value = "每天学习记录")
  		private List<DayDetail> dayDetailList;
  		@ApiModelProperty(value = "学习详情")
  		private LearnTypeMaster learnTypeMaster;
}