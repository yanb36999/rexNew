package com.zmcsoft.rex.learn.impl.service.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.zmcsoft.rex.learn.api.entity.DayDetail;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;

/**
 * 审验学习
 * @author hsweb-generator
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(description = "审验学习")
public class JsonCheckUser {

    @ApiModelProperty(value = "主键")
    @JSONField(name = "ID")
    private String id;

    @ApiModelProperty(value = "反馈状态")
    @JSONField(name = "ZT")
    private String returnStatus;

    @ApiModelProperty(value = "失败原因标志")
    @JSONField(name = "SBYYBZ")
    private String remark;

    @ApiModelProperty(value = "失败原因描述")
    @JSONField(name = "SBYYMS")
    private String errorReason;

    @ApiModelProperty(value = "反馈时间")
    @JSONField(name = "FKSJ")
    private java.util.Date returnTime;

    @ApiModelProperty(value = "微信用户id")
    @JSONField(name = "OPENID")
    private String openId;

}
