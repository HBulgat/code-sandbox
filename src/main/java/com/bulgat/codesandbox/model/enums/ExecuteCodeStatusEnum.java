package com.bulgat.codesandbox.model.enums;


public enum ExecuteCodeStatusEnum {
    EXECUTE_ERROR(1,"Running Error"),
    EXECUTE_SUCCESS(0,"Running Success");
    private final int code;
    private final String message;
    ExecuteCodeStatusEnum(int code,String message){
        this.message=message;
        this.code=code;
    }

    /**
     * 根据 code 获取枚举
     *
     * @param code
     * @return
     */
    public static ExecuteCodeStatusEnum getEnumByCode(int code) {
        for (ExecuteCodeStatusEnum anEnum : ExecuteCodeStatusEnum.values()) {
            if (anEnum.code==code) {
                return anEnum;
            }
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
