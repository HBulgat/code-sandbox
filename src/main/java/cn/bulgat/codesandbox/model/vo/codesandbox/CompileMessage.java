package cn.bulgat.codesandbox.model.vo.codesandbox;

import cn.bulgat.codesandbox.model.enums.CompileCodeStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompileMessage implements Serializable {
    /**
     * 编译状态：编译错误、编译正确
     */
    private CompileCodeStatusEnum compileCodeStatus=CompileCodeStatusEnum.COMPILE_SUCCESS;
    /**
     * 编译执行信息
     */
    private String message;
}
