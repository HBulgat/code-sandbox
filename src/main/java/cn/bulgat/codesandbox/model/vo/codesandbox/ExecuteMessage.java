package cn.bulgat.codesandbox.model.vo.codesandbox;

import cn.bulgat.codesandbox.model.enums.ExecuteCodeStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteMessage implements Serializable {
    /**
     * 编译状态：执行错误、执行正确(默认)
     */
    private ExecuteCodeStatusEnum executeCodeStatus=ExecuteCodeStatusEnum.EXECUTE_SUCCESS;
    /**
     * 执行信息
     */
    private String message;
    /**
     * 执行时间（ms）
     */
    private Long time;
    /**
     * 占用内存（ms）
     */
    private Long memory;
    /**
     * 输出
     */
    private String output;
}
