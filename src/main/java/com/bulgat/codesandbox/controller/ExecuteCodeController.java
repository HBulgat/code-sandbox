package com.bulgat.codesandbox.controller;

import cn.hutool.json.JSONUtil;
import com.bulgat.codesandbox.annotation.ApiAuthCheck;
import com.bulgat.codesandbox.codesandbox.CodeSandbox;
import com.bulgat.codesandbox.common.BaseResponse;
import com.bulgat.codesandbox.common.ErrorCode;
import com.bulgat.codesandbox.common.ResultUtils;
import com.bulgat.codesandbox.exception.BusinessException;
import com.bulgat.codesandbox.model.ExecuteCodeRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/execute_code")
public class ExecuteCodeController {
    @Resource
    private CodeSandbox codeSandbox;

    @PostMapping("/execute_code")
    @ApiAuthCheck
    public BaseResponse<String> executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest, HttpServletRequest request){
        if (executeCodeRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(JSONUtil.toJsonStr(codeSandbox.executeCode(executeCodeRequest)));
    }
}
