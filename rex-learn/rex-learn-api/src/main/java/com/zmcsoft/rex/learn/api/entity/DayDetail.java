package com.zmcsoft.rex.learn.api.entity;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.Date;
import java.util.List;

/**
* 每天学习记录
* @author hsweb-generator
*/
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(description = "每天学习记录")
public class DayDetail extends SimpleGenericEntity<String> {

		//未学习
		public static final Integer DAY_NO= 0;
		//学习中
		public static final Integer DAY_ING= 1;
		//已完成
		public static final Integer DAY_OK=2;

  		@ApiModelProperty(value = "用户登记表id")
        private String userDetailId;
  		@ApiModelProperty(value = "第几天")
        private String dayNo;
  		@ApiModelProperty(value = "开始时间")
        private java.util.Date startTime;
  		@ApiModelProperty(value = "结束时间")
        private java.util.Date endTime;
  		@ApiModelProperty(value = "学习日期")
        private java.util.Date learnDay;
  		@ApiModelProperty(value = "当前学习时长")
        private Long currTime;
  		@ApiModelProperty(value = "学习总时长")
  		private Long countTime;
  		@ApiModelProperty(value = "学习状态0：未开始，1：学习中，2：已完成")
        private Integer status;
  		@ApiModelProperty(value = "学习内容模板id")
        private String contentMasterId;
		@ApiModelProperty(value = "学习完成课件总数")
		private Integer courseCount;
  		@ApiModelProperty(value = "课件学习记录")
  		private List<CourseDetail> courseDetailList;
  		@ApiModelProperty(value = "当天学习详情")
  		private ContentMaster dayMaster;
  		@ApiModelProperty(value = "更新时间")
  		private Date updateTime;
}