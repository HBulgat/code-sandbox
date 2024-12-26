package com.bulgat.codesandbox.controller;

import com.bulgat.codesandbox.codesandbox.CodeSandbox;
import com.bulgat.codesandbox.common.BaseResponse;
import com.bulgat.codesandbox.common.ErrorCode;
import com.bulgat.codesandbox.common.ResultUtils;
import com.bulgat.codesandbox.exception.BusinessException;
import com.bulgat.codesandbox.model.ExecuteCodeResponse;
import com.bulgat.codesandbox.model.ExecuteCodeRequest;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
@RestController
@RequestMapping("/")
public class MainController {
    @Resource
    private CodeSandbox codeSandbox;

    @PostMapping("/execute_code")
    public BaseResponse<ExecuteCodeResponse> executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest){
        if (executeCodeRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(codeSandbox.executeCode(executeCodeRequest));
    }
}
