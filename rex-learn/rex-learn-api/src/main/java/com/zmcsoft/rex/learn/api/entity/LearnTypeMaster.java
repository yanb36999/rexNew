package com.zmcsoft.rex.learn.api.entity;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;

/**
* 学习类型模板
* @author hsweb-generator
*/
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(description = "学习类型模板")
public class LearnTypeMaster extends SimpleGenericEntity<String> {


    //审验学习
    public static final String CHECK_LEARN= "3";
    //满分学习
    public static final String FULL_LEARN= "2";

    @ApiModelProperty(value = "名称")
    private String name;
    @ApiModelProperty(value = "学习内容id")
    private String contentMasterId;
    @ApiModelProperty(value = "最大学习天数")
    private String maxDay;
    @ApiModelProperty(value = "学习内容详情")
    private ContentMaster contentMaster;

}