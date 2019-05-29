package com.zmcsoft.rex.learn.api.entity;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
/**
* 视频信息
* @author hsweb-generator
*/
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(description = "视频信息")
public class VideoMaster extends SimpleGenericEntity<String> {
  		@ApiModelProperty(value = "视频地址")
        private String path;
  		@ApiModelProperty(value = "视频名称")
        private String name;
  		@ApiModelProperty(value = "观看最小时间")
  		private String minTime;
  		@ApiModelProperty(value = "转码后地址")
        private String convertPath;
  		@ApiModelProperty(value = "视频简介")
  		private String remark;

}