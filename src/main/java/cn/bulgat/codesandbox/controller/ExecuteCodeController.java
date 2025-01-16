package cn.bulgat.codesandbox.controller;

import cn.bulgat.codesandbox.codesandbox.CodeSandbox;
import cn.hutool.json.JSONUtil;
import cn.bulgat.codesandbox.annotation.ApiAuthCheck;
import cn.bulgat.codesandbox.common.BaseResponse;
import cn.bulgat.codesandbox.common.ErrorCode;
import cn.bulgat.codesandbox.common.ResultUtils;
import cn.bulgat.codesandbox.exception.BusinessException;
import cn.bulgat.codesandbox.model.vo.codesandbox.ExecuteCodeRequest;
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
