package com.zmcsoft.rex.learn.api.entity;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;

/**
* 每天学习模板
* @author hsweb-generator
*/
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(description = "每天学习模板")
public class ContentMaster extends SimpleGenericEntity<String> {

		//课件内容Id，原天模板Id
		public static final String CONTENT_ID= "18c9ff3e19f5d642aea2af2dfb4ad606";

		public static final String CHECK_CONTENT_ID = "ce55a31b31295b28613b79af2475e0f1";

  		@ApiModelProperty(value = "名称")
        private String name;
  		@ApiModelProperty(value = "课件id集合")
        private List<String> courseIdList;
  		@ApiModelProperty(value = "至少学习时长")
  		private String minTime;
  		@ApiModelProperty(value = "课件详情")
  		private List<CoursewareMaster> coursewareMasterList;
}