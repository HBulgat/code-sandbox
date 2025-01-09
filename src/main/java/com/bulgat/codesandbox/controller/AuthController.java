package com.bulgat.codesandbox.controller;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bulgat.codesandbox.annotation.UserAuthCheck;
import com.bulgat.codesandbox.common.BaseResponse;
import com.bulgat.codesandbox.common.ErrorCode;
import com.bulgat.codesandbox.common.ResultUtils;
import com.bulgat.codesandbox.constant.UserConstant;
import com.bulgat.codesandbox.exception.BusinessException;
import com.bulgat.codesandbox.exception.ThrowUtils;
import com.bulgat.codesandbox.model.dto.auth.AuthAddRequest;
import com.bulgat.codesandbox.model.dto.auth.AuthDeleteRequest;
import com.bulgat.codesandbox.model.entity.Auth;
import com.bulgat.codesandbox.model.entity.User;
import com.bulgat.codesandbox.model.vo.AuthVO;
import com.bulgat.codesandbox.service.AuthService;
import com.bulgat.codesandbox.service.UserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Resource
    private AuthService authService;
    @Resource
    private UserService userService;

    @PostMapping("/add")
    @UserAuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<AuthVO> addAuth(@RequestBody AuthAddRequest authAddRequest, HttpServletRequest request) {
        if (authAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String accessKeyPrefix = authAddRequest.getAccessKeyPrefix();
        if (StrUtil.isBlank(accessKeyPrefix)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"前缀为空");
        }

        String accessKey=accessKeyPrefix+"-"+RandomStringUtils.random(15,true,true);
        String secretKey= RandomStringUtils.random(35,true,true);
        User loginUser=userService.getLoginUser(request);
        Auth auth=new Auth();
        auth.setAccessKey(accessKey);
        auth.setSecretKey(secretKey);
        auth.setUserId(loginUser.getId());
        boolean save = authService.save(auth);
        ThrowUtils.throwIf(!save,ErrorCode.OPERATION_ERROR);
        AuthVO authVO = AuthVO.objToVO(auth);
        authVO.setUserVO(userService.getUserVO(loginUser));
        return ResultUtils.success(authVO);
    }
    @PostMapping("/delete")
    @UserAuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteQuestion(@RequestBody AuthDeleteRequest authDeleteRequest, HttpServletRequest request) {
        if (authDeleteRequest == null ) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String accessKey = authDeleteRequest.getAccessKey();
        if (StrUtil.isBlank(accessKey)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<Auth> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("accessKey",accessKey);
        Auth auth = authService.getOne(queryWrapper);
        ThrowUtils.throwIf(auth==null,ErrorCode.NOT_FOUND_ERROR);
        boolean remove = authService.remove(queryWrapper);
        return ResultUtils.success(remove);
    }
}
