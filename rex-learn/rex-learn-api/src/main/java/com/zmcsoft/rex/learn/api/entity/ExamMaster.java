package com.zmcsoft.rex.learn.api.entity;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;

/**
* 考试题信息
* @author hsweb-generator
*/
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(description = "考试题信息")
public class ExamMaster extends SimpleGenericEntity<String> {
  		@ApiModelProperty(value = "类型")
        private String type;
  		@ApiModelProperty(value = "题目")
        private String topic;
  		@ApiModelProperty(value = "选项,格式：[{A:内容A},{B:内容B},{C:内容C}]")
        private List<Options> examOptions;
        @ApiModelProperty(value = "正确答案 ['A','B']")
        private List<String> answer;
  		@ApiModelProperty(value = "材料地址")
        private List<String> fileUrl;
}