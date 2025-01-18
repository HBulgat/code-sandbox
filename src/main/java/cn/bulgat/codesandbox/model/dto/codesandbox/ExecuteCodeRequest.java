package cn.bulgat.codesandbox.model.dto.codesandbox;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * 请求体
 * 接收：
 * - code
 * - 语言
 * - 输入
 */
public class ExecuteCodeRequest implements Serializable {
    private List<Input> inputList;
    private String code;
    private String language;
}
