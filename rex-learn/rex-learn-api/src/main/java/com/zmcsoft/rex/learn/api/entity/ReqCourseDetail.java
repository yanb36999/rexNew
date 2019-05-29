package com.zmcsoft.rex.learn.api.entity;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(description = "审验学习申请表")
public class ReqCourseDetail extends SimpleGenericEntity<String> {

    @ApiModelProperty(value = "用户Id")
    private String userDetailId;

    @ApiModelProperty(value = "天学习记录Id,如果没有则不传",required = false)
    private String dayDetailId;

    @ApiModelProperty(value = "学习内容类型(0:视频,1:知识点,2:考试)")
    private Integer type;

    @ApiModelProperty(value = "课件学习记录详情")
    private CourseDetail courseDetail;

    @ApiModelProperty(value = "学习类型（LeranType:2满分学习。3审验学习）")
    private String learnType;
}
