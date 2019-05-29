package com.zmcsoft.rex.learn.controller.entity;

import com.zmcsoft.rex.learn.api.entity.CheckUser;
import com.zmcsoft.rex.learn.api.entity.UserDetail;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(description = "历史记录")
public class UserLearnHistory {

    @ApiModelProperty("满分学习历史记录")
    private List<UserDetail> userDetailList;

    @ApiModelProperty("审验学习历史记录")
    private List<CheckUser> checkUserList;
}
