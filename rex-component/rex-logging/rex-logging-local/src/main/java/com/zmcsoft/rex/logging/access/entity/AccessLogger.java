package com.zmcsoft.rex.logging.access.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "访问日志")
public class AccessLogger extends SimpleGenericEntity<String> {

    @ApiModelProperty("所属应用")
    private String app;

    @ApiModelProperty("操作类型")
    private String action;

    @ApiModelProperty("java方法")
    private String methodName;

    @ApiModelProperty("java类名")
    private String className;

    @ApiModelProperty("id地址")
    private String ipAddress;

    @ApiModelProperty("请求头JSON")
    private String httpHeader;

    @ApiModelProperty("请求方法")
    private String httpMethod;

    @ApiModelProperty("请求地址")
    private String requestUrl;

    @ApiModelProperty("请求用户ID")
    private String requestUserId;

    @ApiModelProperty("请求用户名")
    private String requestUserName;

    @ApiModelProperty("请求时间")
    private Long requestTime;

    @ApiModelProperty("响应时间")
    private Long responseTime;

    @ApiModelProperty("请求参数JSON")
    private String parameters;

    @ApiModelProperty("响应结果JSON")
    private String response;


}
