package cn.bulgat.codesandbox.controller;

import cn.bulgat.codesandbox.exception.BusinessException;
import cn.bulgat.codesandbox.exception.ThrowUtils;
import cn.bulgat.codesandbox.model.dto.auth.AuthAddRequest;
import cn.bulgat.codesandbox.model.dto.auth.AuthDeleteRequest;
import cn.bulgat.codesandbox.model.entity.Auth;
import cn.bulgat.codesandbox.model.entity.User;
import cn.bulgat.codesandbox.service.AuthService;
import cn.bulgat.codesandbox.service.UserService;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import cn.bulgat.codesandbox.annotation.UserAuthCheck;
import cn.bulgat.codesandbox.common.BaseResponse;
import cn.bulgat.codesandbox.common.ErrorCode;
import cn.bulgat.codesandbox.common.ResultUtils;
import cn.bulgat.codesandbox.constant.UserConstant;
import cn.bulgat.codesandbox.model.vo.AuthVO;
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
        AuthVO authVO=authService.addAuth(accessKeyPrefix,request);
        return ResultUtils.success(authVO);
    }
    @PostMapping("/delete")
    @UserAuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteAuth(@RequestBody AuthDeleteRequest authDeleteRequest, HttpServletRequest request) {
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
