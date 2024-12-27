package com.bulgat.codesandbox.model.enums;

import lombok.Getter;

@Getter
public enum CmdTypeEnum {
    COMPILE("编译","compile"),
    EXECUTE("运行","execute");
    private String text;
    private String value;

    CmdTypeEnum(String text,String value){
        this.text=text;
        this.value=value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static CmdTypeEnum getEnumByValue(String value) {
        for (CmdTypeEnum anEnum : CmdTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
