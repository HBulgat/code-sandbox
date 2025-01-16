package cn.bulgat.codesandbox.codesandbox;

import cn.bulgat.codesandbox.model.vo.codesandbox.ExecuteCodeResponse;
import cn.bulgat.codesandbox.model.vo.codesandbox.ExecuteCodeRequest;

public interface CodeSandbox {
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
