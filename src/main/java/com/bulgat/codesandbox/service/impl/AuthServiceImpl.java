package com.bulgat.codesandbox.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bulgat.codesandbox.model.entity.Auth;
import com.bulgat.codesandbox.service.AuthService;
import com.bulgat.codesandbox.mapper.AuthMapper;
import org.springframework.stereotype.Service;

/**
* @author bulgat
* @description 针对表【auth(权限表)】的数据库操作Service实现
* @createDate 2025-01-04 18:53:16
*/
@Service
public class AuthServiceImpl extends ServiceImpl<AuthMapper, Auth>
    implements AuthService{

}




