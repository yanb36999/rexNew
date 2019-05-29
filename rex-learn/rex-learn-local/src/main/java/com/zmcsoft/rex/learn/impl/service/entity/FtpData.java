package com.zmcsoft.rex.learn.impl.service.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * 为了配合内网报文做的
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(description = "报文外层")
public class FtpData {

    @ApiModelProperty(value = "message")
    private String message;
    @ApiModelProperty(value = "result")
    private Object result;
    @ApiModelProperty(value = "status")
    private String status;
    @ApiModelProperty(value = "timestamp")
    private String timestamp;
}
