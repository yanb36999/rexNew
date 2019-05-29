package com.zmcsoft.rex.logging.business;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import java.util.Date;

/**
 * 业务日志
 *
 * @author zhouhao
 * @since 1.0
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "业务日志")
public class BusinessLogger extends SimpleGenericEntity<String> {

    @ApiModelProperty("应用")
    private String app;

    @ApiModelProperty("业务名称")
    private String name;

    @ApiModelProperty("请求id")
    private String requestId;

    @ApiModelProperty("线程id")
    private String threadId;

    @ApiModelProperty("线程名称")
    private String threadName;

    @ApiModelProperty("类名")
    private String className;

    @ApiModelProperty("方法名称")
    private String methodName;

    @ApiModelProperty("行号")
    private Integer lineNumber;

    @ApiModelProperty("日志内容")
    private String message;

    @ApiModelProperty("日志事件")
    private Long createTime;

    @ApiModelProperty("日志级别")
    private String level;
}
