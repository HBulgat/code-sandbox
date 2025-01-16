package cn.bulgat.codesandbox.model;

import cn.bulgat.codesandbox.model.enums.CompileCodeStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompileMessage {
    /**
     * 编译状态：编译错误、编译正确
     */
    private CompileCodeStatusEnum compileCodeStatus=CompileCodeStatusEnum.COMPILE_SUCCESS;
    /**
     * 编译执行信息
     */
    private String message;
}
