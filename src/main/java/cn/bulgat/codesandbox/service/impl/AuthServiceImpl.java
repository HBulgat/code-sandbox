package cn.bulgat.codesandbox.service.impl;

import cn.bulgat.codesandbox.common.ErrorCode;
import cn.bulgat.codesandbox.config.ApiSecurityConfig;
import cn.bulgat.codesandbox.exception.ThrowUtils;
import cn.bulgat.codesandbox.model.entity.Auth;
import cn.bulgat.codesandbox.model.entity.User;
import cn.bulgat.codesandbox.model.vo.auth.AuthVO;
import cn.bulgat.codesandbox.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.bulgat.codesandbox.service.AuthService;
import cn.bulgat.codesandbox.mapper.AuthMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
* @author bulgat
* @description 针对表【auth(权限表)】的数据库操作Service实现
* @createDate 2025-01-04 18:53:16
*/
@Service
public class AuthServiceImpl extends ServiceImpl<AuthMapper, Auth>
    implements AuthService{
    @Resource
    private ApiSecurityConfig apiSecurityConfig;

    @Resource
    @Lazy
    private UserService userService;
    @Override
    public boolean isExpire(Long timestamp) {
        long currentTimeMillis=System.currentTimeMillis();
        return currentTimeMillis-timestamp>apiSecurityConfig.getNonce().getExpireTime();
    }

    @Override
    public AuthVO addAuth(String accessKeyPrefix, HttpServletRequest request) {
        String accessKey=accessKeyPrefix+"-"+ RandomStringUtils.random(25,true,true);
        String secretKey= RandomStringUtils.random(55,true,true);
        User loginUser=userService.getLoginUser(request);
        Auth auth=new Auth();
        auth.setAccessKey(accessKey);
        auth.setSecretKey(secretKey);
        auth.setUserId(loginUser.getId());
        boolean save = this.save(auth);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR);
        AuthVO authVO = AuthVO.objToVO(auth);
        authVO.setUserVO(userService.getUserVO(loginUser));
        return authVO;
    }

}




