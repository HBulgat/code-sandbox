package cn.bulgat.codesandbox.codesandbox;

import cn.bulgat.codesandbox.model.ExecuteCodeResponse;
import cn.bulgat.codesandbox.model.ExecuteCodeRequest;

public interface CodeSandbox {
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
