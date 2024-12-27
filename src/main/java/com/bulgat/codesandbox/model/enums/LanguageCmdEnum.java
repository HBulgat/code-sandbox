package com.bulgat.codesandbox.model.enums;

import com.bulgat.codesandbox.common.Constant;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 编程语言 cmd 枚举
 * 不需要编译的语言编译的 cmd 设置为空即可
 */
@Getter
public enum LanguageCmdEnum {

    JAVA("java","Main.java",new String[]{"javac","-encoding","utf-8","Main.java"},new String[]{"/bin/sh", "-c","java -Dfile.encoding=UTF-8 -cp /box Main"},new String[]{"/bin/sh", "-c","java -Dfile.encoding=UTF-8 -cp /box Main <"}),
    CPP("cpp", "main.cpp", new String[]{"g++", "-finput-charset=UTF-8", "-fexec-charset=UTF-8", "-o", "main", "main.cpp"}, new String[]{"/bin/sh", "-c", "./main"},new String[]{"/bin/sh", "-c","./main <"}),
    C("c", "main.c", new String[]{"gcc", "-finput-charset=UTF-8", "-fexec-charset=UTF-8", "-o", "main", "main.c"}, new String[]{"/bin/sh", "-c","./main"}, new String[]{"/bin/sh", "-c","./main <"}),
    PYTHON3("python", "main.py", null, new String[]{"/bin/sh", "-c","python3 main.py"},new String[]{"/bin/sh", "-c","python3 main.py <"}),
    JAVASCRIPT("javascript", "main.js", null, new String[]{"/bin/sh", "-c","node main.js"},new String[]{"/bin/sh", "-c","node main.js <"}),
    TYPESCRIPT("typescript", "main.ts", null, new String[]{"/bin/sh", "-c","node main.ts"}, new String[]{"/bin/sh", "-c","node main.ts <"}),
//    GO("go", "main.go", null, new String[]{"/bin/sh", "-c","go run main.go"}, new String[]{"/bin/sh", "-c","go run main.go <"});
    ;
    private final String language;

    /**
     * 保存的文件名
     */
    private final String saveFileName;

    /**
     * 编译命令
     */
    private final String[] compileCmd;
    /**
     * 执行命令（没有输入）
     */
    private final String[] runCmdWithNoInput;
    /**
     * 执行命令（有输入）
     */
    private final String[] runCmdWithInput;

    LanguageCmdEnum(String language, String saveFileName, String[] compileCmd, String[] runCmdWithNoInput,String[] runCmdWithInput) {
        this.language = language;
        this.saveFileName = saveFileName;
        this.compileCmd = compileCmd;
        this.runCmdWithNoInput = runCmdWithNoInput;
        this.runCmdWithInput=runCmdWithInput;
    }

    /**
     * 根据 language 获取枚举
     *
     * @param language 值
     * @return {@link LanguageCmdEnum}
     */
    public static LanguageCmdEnum getEnumByLanguage(String language) {
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

