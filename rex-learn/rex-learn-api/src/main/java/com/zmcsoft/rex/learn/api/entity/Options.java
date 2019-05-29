package com.zmcsoft.rex.learn.api.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(description = "考试题选项")
public class Options {

    @ApiModelProperty(value = "选项")
    private String option;

    @ApiModelProperty(value = "内容")
    private String content;
}
