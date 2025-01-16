package cn.bulgat.codesandbox.service;

import cn.bulgat.codesandbox.model.entity.Auth;
import cn.bulgat.codesandbox.model.vo.AuthVO;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
* @author bulgat
* @description 针对表【auth(权限表)】的数据库操作Service
* @createDate 2025-01-04 18:53:16
*/
public interface AuthService extends IService<Auth> {
    boolean isExpire(Long timestamp);


    AuthVO addAuth(String accessKeyPrefix, HttpServletRequest request);
}
