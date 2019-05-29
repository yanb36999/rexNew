package com.zmcsoft.rex.learn.impl.service.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.zmcsoft.rex.learn.api.entity.DayDetail;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

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
public class JsonUserDetail extends SimpleGenericEntity<String> {

		@JSONField(name = "jszh")
		@ApiModelProperty(value = "驾驶证号")
		private String licenseNo;

		@JSONField(name = "xxlx")
		@ApiModelProperty(value = "学习类型")
		private String type;

		@JSONField(name = "dabh")
		@ApiModelProperty(value = "档案编号")
		private String fileNo;

		@JSONField(name = "xxyy")
		@ApiModelProperty(value = "学习原因")
		private String reason;

		@JSONField(name = "code")
		@ApiModelProperty(value = "公安内网生成的唯一标识")
		private String code;

		@JSONField(name = "bs")
		@ApiModelProperty(value = "标识：0：无效；1：有效")
		private Integer sign;

		@JSONField(name = "zjcx")
		@ApiModelProperty(value = "准驾车型")
		private String driverType;

		@JSONField(name = "dsr")
		@ApiModelProperty(value = "当事人")
		private String parties;

		@JSONField(name = "yhid")
		@ApiModelProperty(value = "内网用户Id")
		private String userDetailId;

		@JSONField(name = "yyrq")
		@ApiModelProperty(value = "预约现场学习日期格式：2017-11-25|2017-11-26|2017-11-27")
		private String offlineLearnTime;

		@JSONField(name = "jbr")
		@ApiModelProperty(value = "登记人(经办人)")
		private String registrant;

		@JSONField(name = "lxdh")
		@ApiModelProperty(value = "联系电话")
		private String phone;

		@JSONField(name = "rksj")
		@ApiModelProperty(value = "入库时间")
		private java.util.Date registerTime;

		@JSONField(name = "ssbm")
		@ApiModelProperty(value = "学习部门")
		private String dept;

		@JSONField(name = "zt")
		@ApiModelProperty(value = "状态 0：未学习；1：学习中；2：已完成")
		private Integer status;

		@JSONField(name = "yyts")
		@ApiModelProperty(value = "网上预约学习天数")
		private Integer onlineLearnDaySum;

		@JSONField(name = "ssbmmc")
		@ApiModelProperty(value = "学习部门名称")
		private String deptName;

		@ApiModelProperty(value = "发证机关")
		@JSONField(name = "fzjg")
		private String sendOffice;
}