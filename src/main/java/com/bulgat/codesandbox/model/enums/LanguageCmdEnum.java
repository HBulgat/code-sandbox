package com.bulgat.codesandbox.model.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 编程语言 cmd 枚举
 * 不需要编译的语言编译的 cmd 设置为空即可
 */
@Getter
public enum LanguageCmdEnum {
    ;

    private final String language;

    /**
     * 保存的文件名
     */
    private final String saveFileName;

    private final String[] compileCmd;

    private final String[] runCmd;


    LanguageCmdEnum(String language, String saveFileName, String[] compileCmd, String[] runCmd) {
        this.language = language;
        this.saveFileName = saveFileName;
        this.compileCmd = compileCmd;
        this.runCmd = runCmd;
    }

    /**
     * 根据 language 获取枚举
     *
     * @param language 值
     * @return {@link LanguageCmdEnum}
     */
    public static LanguageCmdEnum getEnumByValue(String language) {
        if (StringUtils.isBlank(language)) {
            return null;
        }
        for (LanguageCmdEnum languageCmdEnum : LanguageCmdEnum.values()) {
            if (languageCmdEnum.language.equals(language)) {
                return languageCmdEnum;
            }
        }
        return null;
    }
}

