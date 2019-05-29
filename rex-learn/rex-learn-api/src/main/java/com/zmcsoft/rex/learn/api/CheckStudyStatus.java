package com.zmcsoft.rex.learn.api;

import java.util.Arrays;


/**
 * 审验学习状态
 */
public enum CheckStudyStatus {

    REQUEST("5001", "未处理"),
    COMMIT("5002", "申请中"),
    APPLY_FAIL("5003", "申请失败"),
    APPLY_OK("5004", "申请成功"),
    CANCEL("5005","超时作废");

    private String code;

    private String message;

    CheckStudyStatus(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }

    public static CheckStudyStatus ofCode(String code) {
        return Arrays.stream(values()).filter(status -> status.code().equals(code))
                .findFirst().orElse(null);
    }

    public boolean eq(String code){
        return  code().equals(code);
    }
}
