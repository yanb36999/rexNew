package com.zmcsoft.rex.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(description = "用户车辆违法信息")
@ToString
public class PayDetail extends SimpleGenericEntity<String> {

    @ApiModelProperty(value = "订单号", required = true)
    private String  paySerialId;

    @ApiModelProperty(value = "流水号")
    private String channelSerialId;

    @ApiModelProperty(value = "渠道号")
    private String channelId;

    @ApiModelProperty(value = "金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "回调地址", required = true)
    private String  callbackUrl ;

    @ApiModelProperty(value = "回调状态")
    private String callbackStatus ;

    @ApiModelProperty(value = "支付回调地址")
    private String  payReturnUrl;

    @ApiModelProperty(value = "支付回调时间")
    private Date payReturnTime;

    @ApiModelProperty(value = "支付状态")
    private String  payStatus;

    @ApiModelProperty(value = "支付状态说明")
    private String  payStatusRemark ;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "备注")
    private String remark ;

    @ApiModelProperty(value = "摘要")
    private String summary ;

    @ApiModelProperty(value = "回调数据")
    private String callbackData;

}