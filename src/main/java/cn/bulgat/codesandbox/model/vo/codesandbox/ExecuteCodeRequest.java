package cn.bulgat.codesandbox.model.vo.codesandbox;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class ExecuteCodeRequest {
    private List<String> inputList;
    private String code;
    private String language;
}
