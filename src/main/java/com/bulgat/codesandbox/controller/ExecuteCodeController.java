package com.bulgat.codesandbox.controller;

import cn.hutool.db.sql.Query;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bulgat.codesandbox.codesandbox.CodeSandbox;
import com.bulgat.codesandbox.common.BaseResponse;
import com.bulgat.codesandbox.common.Constant;
import com.bulgat.codesandbox.common.ErrorCode;
import com.bulgat.codesandbox.common.ResultUtils;
import com.bulgat.codesandbox.exception.BusinessException;
import com.bulgat.codesandbox.model.Auth;
import com.bulgat.codesandbox.model.ExecuteCodeRequest;
import com.bulgat.codesandbox.model.ExecuteCodeResponse;
import com.bulgat.codesandbox.service.AuthService;
import org.apache.commons.lang3.StringUtils;
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

    @Resource
    private AuthService authService;

    @PostMapping("/execute_code")
    public BaseResponse<ExecuteCodeResponse> executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest, HttpServletRequest request){
        doAuthCheck(request);
        if (executeCodeRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(codeSandbox.executeCode(executeCodeRequest));
    }

    private void doAuthCheck(HttpServletRequest request){
        if (request==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String accessKey = request.getHeader(Constant.AUTH_HEADER_ACCESS_KEY);
        String secretKey = request.getHeader(Constant.AUTH_HEADER_SECRET_KEY);
        if (StringUtils.isAnyBlank(accessKey,secretKey)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        QueryWrapper<Auth> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq(Constant.AUTH_HEADER_ACCESS_KEY,accessKey);
        Auth auth = authService.getOne(queryWrapper);
        if (auth==null||!auth.getSecretKey().equals(secretKey)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
    }
}
