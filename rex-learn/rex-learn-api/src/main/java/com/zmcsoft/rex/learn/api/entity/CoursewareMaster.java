package com.zmcsoft.rex.learn.api.entity;
import io.swagger.annotations.Api;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;

/**
* 课件模板
* @author hsweb-generator
*/
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(description = "课件模板")
public class CoursewareMaster extends SimpleGenericEntity<String> {

		//视频
		public static final Integer COURSE_VIDEO= 1;
		//知识点
		public static final Integer COURSE_KNOW= 2;
		//考试
		public static final Integer COURSE_EXAM=3;


		@ApiModelProperty(value = "课件名称")
		private String name;
		@ApiModelProperty(value = "课件编号")
		private String code;
		@ApiModelProperty(value = "课件类型")
		private CourseType courseType;
  		@ApiModelProperty(value = "视频Id")
        private List<String> videoIdList;
  		@ApiModelProperty(value = "知识点id")
        private List<String> knowledgeIdList;
  		@ApiModelProperty(value = "试题Id")
        private List<String> examIdList;

  		@ApiModelProperty(value = "考试时长")
        private String examMaxTime;
  		@ApiModelProperty(value = "考试题数量")
        private Integer examUnitCount;
  		@ApiModelProperty(value = "考试题抽取规则")
        private Integer examUnitRule;

		@ApiModelProperty(value = "封面地址")
		private String picture;
  		@ApiModelProperty(value = "课件最小学习时间")
  		private Long courseMinTime;
  		@ApiModelProperty(value = "是否必学")
  		private Boolean must;

  		@ApiModelProperty(value = "知识点详情")
  		private List<KnowledgeMaster> knowledgeMasterList;

  		@ApiModelProperty(value = "视频详情")
  		private List<VideoMaster> videoMasterList;

  		@ApiModelProperty(value = "考试题详情")
  		private List<ExamMaster> examMasterList;

  		@ApiModelProperty(value = "创建时间")
        private java.util.Date createTime;
  		@ApiModelProperty(value = "更新时间")
        private java.util.Date updateTime;
  		@ApiModelProperty(value = "创建人")
        private String createUser;
  		@ApiModelProperty(value = "更新人")
        private String updateUser;
}