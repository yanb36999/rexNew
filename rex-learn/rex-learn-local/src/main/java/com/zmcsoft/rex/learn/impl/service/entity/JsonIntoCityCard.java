package com.zmcsoft.rex.learn.impl.service.entity;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

/**
* 入城证
* @author hsweb-generator
*/
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(description = "入城证报文")
public class JsonIntoCityCard extends SimpleGenericEntity<String> {

  		@ApiModelProperty(value = "用户id")
        @JSONField(name = "openid")
        private String openId;

        @JSONField(name = "c_yhxm")
        @ApiModelProperty(value = "用户姓名")
        private String userName;

        @JSONField(name = "c_lxdh")
        @ApiModelProperty(value = "联系电话")
        private String phone;

        @JSONField(name = "czmc")
        @ApiModelProperty(value = "车主姓名")
        private String carOwner;

        @JSONField(name = "c_hpzl")
        @ApiModelProperty(value = "车牌种类,01大型车，02小型车")
        private String plateType;

        @JSONField(name = "c_hphm")
        @ApiModelProperty(value = "车牌号码")
        private String plateNumber;

        @JSONField(name = "c_jdcxh")
  		@ApiModelProperty(value = "车架号")
        private String vin;

        @JSONField(name = "c_rczlx")
  		@ApiModelProperty(value = "入城证类型")
        private String type;

        @JSONField(name = "c_rczhm")
        @ApiModelProperty(value = "入城证号码")
        private String cardNo;

        @JSONField(name = "n_nd")
  		@ApiModelProperty(value = "申领年度")
        private String applyYear;

        @JSONField(name = "d_bmrq")
  		@ApiModelProperty(value = "报名时间")
        private java.util.Date applyTime;

        @JSONField(name = "d_fzrq")
  		@ApiModelProperty(value = "发证时间")
        private java.util.Date sendTime;

        @JSONField(name = "c_ret")
  		@ApiModelProperty(value = "申领状态（2：审核中，0：申领成功，1：申领失败）")
        private Integer applyStatus;

        @JSONField(name = "c_msg")
        @ApiModelProperty(value = "备注")
        private String remark;

        @JSONField(name = "c_sblx")
  		@ApiModelProperty(value = "失败类型")
        private String errorType;

        @JSONField(name = "c_inf")
  		@ApiModelProperty(value = "失败原因")
        private String errorReason;

        @JSONField(name = "d_cjsj")
  		@ApiModelProperty(value = "创建时间")
        private java.util.Date createTime;
        @JSONField(name = "d_gxsj")
  		@ApiModelProperty(value = "更新时间")
        private java.util.Date updateTime;

}