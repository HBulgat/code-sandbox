package cn.bulgat.codesandbox.model.enums;

import lombok.Getter;

@Getter
public enum InputTypeEnum {
    FILE("文件","file"),
    TEXT("文本","text")
    ;
    private final String text;
    private final String value;
    InputTypeEnum(String text,String value){
        this.text=text;
        this.value=value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static InputTypeEnum getEnumByValue(String value) {
        for (InputTypeEnum anEnum : InputTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
