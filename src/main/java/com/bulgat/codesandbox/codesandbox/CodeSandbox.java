package com.bulgat.codesandbox.codesandbox;

import com.bulgat.codesandbox.model.ExecuteCodeResponse;
import com.bulgat.codesandbox.model.ExecuteCodeRequest;

import java.util.List;

public interface CodeSandbox {
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
