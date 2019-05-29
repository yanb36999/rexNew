package com.zmcsoft.rex.learn.api.entity;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
/**
* 课件学习记录
* @author hsweb-generator
*/
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(description = "课件学习记录")
public class CourseDetail extends SimpleGenericEntity<String> {

		//未学习
		public static final Integer COURSE_NO= 0;
		//学习中
		public static final Integer COURSE_ING= 1;
		//已完成
		public static final Integer COURSE_OK=2;


  		@ApiModelProperty(value = "关联用户登记表id")
        private String userDetailId;
  		@ApiModelProperty(value = "学习内容Id")
        private String contentId;
  		@ApiModelProperty(value = "课件ID")
        private String courseId;
		@ApiModelProperty(value = "课件编号")
  		private String courseCode;
  		@ApiModelProperty(value = "课件名称")
  		private String courseName;
  		@ApiModelProperty(value = "课件时长")
        private Long currTime;

  		@ApiModelProperty(value = "视频用户头像图片地址")
        private String videoUserImgPath;
		@ApiModelProperty(value = "视频用户头像图片Base64")
		private String videoUserImgPathBase64;

  		@ApiModelProperty(value = "视频用户身份证图片地址")
        private String videoIdcardImgPath;
		@ApiModelProperty(value = "视频用户身份证图片地址")
		private String videoIdcardImgPathBase64;
		@ApiModelProperty(value = "考试用户头像图片地址")
		private String examUserImgPath;
		@ApiModelProperty(value = "考试用户头像图片地址")
		private String examUserImgPathBase64;
		@ApiModelProperty(value = "考试用户身份证图片地址")
		private String examIdcardImgPath;
		@ApiModelProperty(value = "考试用户身份证图片地址")
		private String examIdcardImgPathBase64;
//		@ApiModelProperty(value = "知识点头像图片地址")
//		private String knowUserImgPathBase64;
//		@ApiModelProperty(value = "知识点头像图片地址")
//		private String knowUserImgPath;
//		@ApiModelProperty(value = "知识点身份证图片地址")
//		private String knowIdcardImgPathBase64;
//		@ApiModelProperty(value = "知识点身份证图片地址")
//		private String knowIdcardImgPath;

		@ApiModelProperty(value = "学习日期")
        private java.util.Date learnDate;
  		@ApiModelProperty(value = "开始时间")
        private java.util.Date startTime;
  		@ApiModelProperty(value = "结束时间")
        private java.util.Date endTime;
//  		@ApiModelProperty(value = "排序")
//        private Integer orderNo;
  		@ApiModelProperty(value = "状态  0：未学习，1：学习中，2：已完成")
        private Integer status;
  		@ApiModelProperty(value = "知识点学习状态；0：未学习，1：学习中，2：已完成")
        private Integer knowledgeStatus;
  		@ApiModelProperty(value = "考试学习状态；0：未学习，1：学习中，2：已完成")
        private Integer examStatus;
  		@ApiModelProperty(value = "视频学习状态；0：未学习，1：学习中，2：已完成")
        private Integer videoStatus;
  		@ApiModelProperty(value = "学习天记录Id")
  		private String dayDetailId;
  		@ApiModelProperty(value = "课件详情")
        private CoursewareMaster coursewareMaster;
}