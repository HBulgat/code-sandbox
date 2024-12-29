package com.bulgat.codesandbox.mapper;

import com.bulgat.codesandbox.model.Auth;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author bulgat
* @description 针对表【auth(权限表)】的数据库操作Mapper
* @createDate 2024-12-28 17:52:04
* @Entity com.bulgat.codesandbox.model.Auth
*/
@Mapper
public interface AuthMapper extends BaseMapper<Auth> {

}




