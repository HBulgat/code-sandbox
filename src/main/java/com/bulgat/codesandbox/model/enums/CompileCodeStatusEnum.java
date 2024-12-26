package com.bulgat.codesandbox.model.enums;

public enum CompileCodeStatusEnum {
    COMPILE_ERROR(1,"Compile Error"),
    COMPILE_SUCCESS(0,"Compile Success");
    private final int code;
    private final String message;
    CompileCodeStatusEnum(int code,String message){
        this.message=message;
        this.code=code;
    }

    /**
     * 根据 code 获取枚举
     *
     * @param code
     * @return
     */
    public static CompileCodeStatusEnum getEnumByCode(int code) {
        for (CompileCodeStatusEnum anEnum : CompileCodeStatusEnum.values()) {
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
