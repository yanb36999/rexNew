package com.zmcsoft.rex.commons.district.api.entity;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
/**
* 道路信息
* @author hsweb-generator
*/
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(description = "道路信息")
public class Road extends SimpleGenericEntity<String> {
  		@ApiModelProperty(value = "行政区划id")
        private String districtId;
  		@ApiModelProperty(value = "行政区划代码")
        private String districtCode;
  		@ApiModelProperty(value = "道路名称")
        private String name;
  		@ApiModelProperty(value = "道路编码")
        private String code;
  		@ApiModelProperty(value = "道路类型")
        private String type;
  		@ApiModelProperty(value = "出警人")
        private String police;
  		@ApiModelProperty(value = "创建时间")
        private java.util.Date createTime;
  		@ApiModelProperty(value = "修改时间")
        private java.util.Date updateTime;
  		@ApiModelProperty(value = "道路全称")
        private String fullName;
}