package com.zmcsoft.rex.learn.api.entity;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
/**
* 考试类型
* @author hsweb-generator
*/
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(description = "考试类型")
public class ExamTypeMaster extends SimpleGenericEntity<String> {
  		@ApiModelProperty(value = "名称")
        private String name;
}