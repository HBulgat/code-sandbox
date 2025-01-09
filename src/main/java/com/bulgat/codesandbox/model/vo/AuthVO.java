package com.bulgat.codesandbox.model.vo;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.*;
import com.bulgat.codesandbox.model.entity.Auth;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class AuthVO implements Serializable {

    private Long id;

    /**
     * accessKey
     */
    private String accessKey;

    /**
     * secretKey
     */
    private String secretKey;

    /**
     * 创建者 id
     */
    private Long userId;

    private UserVO userVO;

    private static final long serialVersionUID = 1L;

    public static AuthVO objToVO(Auth auth){
        if (auth == null) {
            return null;
        }
        AuthVO authVO = new AuthVO();
        BeanUtils.copyProperties(auth, authVO);
        return authVO;
    }
}