package com.zmcsoft.rex.learn.api.entity;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
/**
* 课件类型
* @author hsweb-generator
*/
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(description = "课件类型")
public class CourseType extends SimpleGenericEntity<String> {
  		@ApiModelProperty(value = "名称")
        private String name;
}