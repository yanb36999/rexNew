package com.zmcsoft.rex.learn.api.entity;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
/**
* 知识点
* @author hsweb-generator
*/
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(description = "知识点")
public class KnowledgeMaster extends SimpleGenericEntity<String> {
  		@ApiModelProperty(value = "标题")
        private String title;
  		@ApiModelProperty(value = "内容")
        private String content;
  		@ApiModelProperty(value = "最小阅读时间")
        private String minTime;

}